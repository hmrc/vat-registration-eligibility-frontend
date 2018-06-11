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
import forms.ApplicationUKNinoFormProvider
import identifiers.ApplicationUKNinoId
import models.NormalMode
import utils.{Navigator, UserAnswers}
import views.html.applicationUKNino

import scala.concurrent.Future

class ApplicationUKNinoController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         dataCacheConnector: DataCacheConnector,
                                         navigator: Navigator,
                                         identify: CacheIdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: ApplicationUKNinoFormProvider) extends FrontendController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad() = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.applicationUKNino match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(applicationUKNino(appConfig, preparedForm, NormalMode))
  }

  def onSubmit() = (identify andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(applicationUKNino(appConfig, formWithErrors, NormalMode))),
        (value) =>
          dataCacheConnector.save[Boolean](request.internalId, ApplicationUKNinoId.toString, value).map(cacheMap =>
            Redirect(navigator.nextPage(ApplicationUKNinoId, NormalMode)(new UserAnswers(cacheMap))))
      )
  }
}
