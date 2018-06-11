/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import connectors.DataCacheConnector
import controllers.actions._
import config.FrontendAppConfig
import forms.CompletionCapacityFormProvider
import identifiers.CompletionCapacityId
import models.{CompletionCapacity, Mode, NormalMode}
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.completionCapacity

import scala.concurrent.Future

class CompletionCapacityController @Inject()(
                                        appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        dataCacheConnector: DataCacheConnector,
                                        navigator: Navigator,
                                        identify: CacheIdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: CompletionCapacityFormProvider) extends FrontendController with I18nSupport with Enumerable.Implicits {

  val form = formProvider()

  def onPageLoad() = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.completionCapacity match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(completionCapacity(appConfig, preparedForm, NormalMode))
  }

  def onSubmit() = (identify andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(completionCapacity(appConfig, formWithErrors, NormalMode))),
        (value) =>
          dataCacheConnector.save[CompletionCapacity](request.internalId, CompletionCapacityId.toString, value).map(cacheMap =>
            Redirect(navigator.nextPage(CompletionCapacityId, NormalMode)(new UserAnswers(cacheMap))))
      )
  }
}
