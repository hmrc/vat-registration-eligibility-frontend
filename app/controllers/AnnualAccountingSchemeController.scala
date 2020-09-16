/*
 * Copyright 2020 HM Revenue & Customs
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

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.actions._
import forms.AnnualAccountingSchemeFormProvider
import identifiers.AnnualAccountingSchemeId
import javax.inject.Inject
import models.NormalMode
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import views.html.annualAccountingScheme

import scala.concurrent.Future

class AnnualAccountingSchemeController @Inject()(override val messagesApi: MessagesApi,
                                                 dataCacheConnector: DataCacheConnector,
                                                 navigator: Navigator,
                                                 identify: CacheIdentifierAction,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 formProvider: AnnualAccountingSchemeFormProvider
                                                )(implicit appConfig: FrontendAppConfig) extends FrontendController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.annualAccountingScheme match {
        case None => formProvider()
        case Some(value) => formProvider().fill(value)
      }
      Ok(annualAccountingScheme(preparedForm, NormalMode))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      formProvider().bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(annualAccountingScheme(formWithErrors, NormalMode))),
        (value) =>
          dataCacheConnector.save[Boolean](request.internalId, AnnualAccountingSchemeId.toString, value).map(cacheMap =>
            Redirect(navigator.nextPage(AnnualAccountingSchemeId, NormalMode)(new UserAnswers(cacheMap))))
      )
  }
}