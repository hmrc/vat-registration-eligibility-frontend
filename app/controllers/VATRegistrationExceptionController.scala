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
import controllers.actions._
import featureswitch.core.config.{ExceptionExemptionFlow, FeatureSwitching}
import forms.VATRegistrationExceptionFormProvider
import identifiers.VATRegistrationExceptionId

import javax.inject.{Inject, Singleton}
import models.NormalMode
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{SessionService, TrafficManagementService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.{Navigator, UserAnswers}
import views.html.VatRegistrationException

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VATRegistrationExceptionController @Inject()(mcc: MessagesControllerComponents,
                                                   sessionService: SessionService,
                                                   navigator: Navigator,
                                                   identify: CacheIdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   formProvider: VATRegistrationExceptionFormProvider,
                                                   trafficManagementService: TrafficManagementService,
                                                   view: VatRegistrationException
                                                  )(implicit appConfig: FrontendAppConfig, executionContext: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with FeatureSwitching {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.vatRegistrationException match {
        case None => formProvider()
        case Some(value) => formProvider().fill(value)
      }
      Ok(view(preparedForm, NormalMode))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      formProvider().bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, NormalMode))),
        value => {
          sessionService.save[Boolean](VATRegistrationExceptionId.toString, value).map { cacheMap =>
            if (value && !isEnabled(ExceptionExemptionFlow)) {
              trafficManagementService.upsertRegistrationInformation(request.internalId, request.regId, isOtrs = true)
            }
            Redirect(navigator.nextPage(VATRegistrationExceptionId, NormalMode)(new UserAnswers(cacheMap)))
          }}
      )
  }
}
