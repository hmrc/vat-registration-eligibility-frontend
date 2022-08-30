/*
 * Copyright 2022 HM Revenue & Customs
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
import controllers.actions.{CacheIdentifierAction, DataRequiredAction, DataRetrievalAction, VatRegLanguageSupport}
import featureswitch.core.config.{FeatureSwitching, VATGroupFlow}
import forms.RegistrationReasonFormProvider
import identifiers._
import models._
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{DataCleardownService, SessionService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.{Navigator, UserAnswers}
import views.html.RegistrationReasonView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationReasonController @Inject()(mcc: MessagesControllerComponents,
                                             sessionService: SessionService,
                                             navigator: Navigator,
                                             identify: CacheIdentifierAction,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             formProvider: RegistrationReasonFormProvider,
                                             view: RegistrationReasonView,
                                             dataCleardownService: DataCleardownService
                                            )(implicit appConfig: FrontendAppConfig,
                                              executionContext: ExecutionContext)
  extends FrontendController(mcc) with VatRegLanguageSupport with FeatureSwitching {

  def showVatGroup(isUkCompany: Boolean): Boolean = isUkCompany && isEnabled(VATGroupFlow)

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.registrationReason match {
        case None => formProvider()
        case Some(value) => formProvider().fill(value)
      }
      Ok(view(
        preparedForm,
        NormalMode,
        request.userAnswers.isPartnership,
        showVatGroup(request.userAnswers.isUkCompany),
        request.userAnswers.isOverseas
      ))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      formProvider().bindFromRequest().fold(
        formWithErrors =>
          Future.successful(
            BadRequest(
              view(
                formWithErrors,
                NormalMode,
                request.userAnswers.isPartnership,
                showVatGroup(request.userAnswers.isUkCompany),
                request.userAnswers.isOverseas
              )
            )
          ),
        value =>
          for {
            _ <- dataCleardownService.cleardownOnAnswerChange(value, RegistrationReasonId)
            cacheMap <- sessionService.save[RegistrationReason](RegistrationReasonId.toString, value)
            redirect = Redirect(navigator.nextPage(RegistrationReasonId, NormalMode)(new UserAnswers(cacheMap)))
          } yield redirect
      )
  }
}
