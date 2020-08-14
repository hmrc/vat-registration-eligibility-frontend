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
import forms.RacehorsesFormProvider
import identifiers.RacehorsesId
import javax.inject.Inject
import models.NormalMode
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import services.VatRegistrationService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import views.html.racehorses

import scala.concurrent.{ExecutionContext, Future}

class RacehorsesController @Inject()(appConfig: FrontendAppConfig,
                                     override val messagesApi: MessagesApi,
                                     dataCacheConnector: DataCacheConnector,
                                     navigator: Navigator,
                                     identify: CacheIdentifierAction,
                                     getData: DataRetrievalAction,
                                     requireData: DataRequiredAction,
                                     formProvider: RacehorsesFormProvider,
                                     vatRegistrationService: VatRegistrationService) extends FrontendController with I18nSupport {
  val frontendUrl       = s"${appConfig.vatRegFEURL}${appConfig.vatRegFEURI}${appConfig.vatRegFEFirstPage}"

  val form: Form[Boolean] = formProvider()

  def onPageLoad() = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.racehorses match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(racehorses(appConfig, preparedForm, NormalMode))
  }

  def onSubmit() = (identify andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(racehorses(appConfig, formWithErrors, NormalMode))),
        (value) =>
          dataCacheConnector.save[Boolean](request.internalId, RacehorsesId.toString, value).flatMap(_ =>
            vatRegistrationService.submitEligibility(request.internalId)(hc, implicitly[ExecutionContext], request)
              .map(_ => Redirect(frontendUrl))
          )
      )
  }
}
