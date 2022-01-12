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
import connectors.SessionService
import controllers.actions._
import forms.ThresholdInTwelveMonthsFormProvider
import identifiers.{TaxableSuppliesInUkId, ThresholdInTwelveMonthsId, ThresholdNextThirtyDaysId, ThresholdPreviousThirtyDaysId, VATRegistrationExceptionId, VoluntaryRegistrationId}
import models.{ConditionalDateFormElement, NETP, NormalMode, Overseas, RegistrationInformation}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.TrafficManagementService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.{Navigator, UserAnswers}
import views.html.thresholdInTwelveMonths

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ThresholdInTwelveMonthsController @Inject()(mcc: MessagesControllerComponents,
                                                  sessionService: SessionService,
                                                  navigator: Navigator,
                                                  identify: CacheIdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  formProvider: ThresholdInTwelveMonthsFormProvider,
                                                  trafficManagementService: TrafficManagementService,
                                                  view: thresholdInTwelveMonths
                                                 )(implicit appConfig: FrontendAppConfig,
                                                   executionContext: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      (request.userAnswers.fixedEstablishment, request.userAnswers.registeringBusiness, request.userAnswers.businessEntity) match {  //TODO Move the journey continue logic into a separate controller: SAR-8201
        case (Some(true), Some(_), _) =>
          val preparedForm = request.userAnswers.thresholdInTwelveMonths match {
            case None => formProvider()
            case Some(value) => formProvider().fill(value)
          }
          Ok(view(preparedForm, NormalMode, request.userAnswers.isPartnership))
        case (Some(false), Some(_), Some(NETP | Overseas)) =>
          Redirect(navigator.pageIdToPageLoad(TaxableSuppliesInUkId))
        case _ => Redirect(routes.IntroductionController.onPageLoad)
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      formProvider().bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, NormalMode))),
        formValue =>
          sessionService.save[ConditionalDateFormElement](request.internalId, ThresholdInTwelveMonthsId.toString, formValue).flatMap { _ =>
            if (formValue.value) {
              sessionService.removeEntry(request.internalId, VoluntaryRegistrationId.toString).flatMap {
                _ => sessionService.removeEntry(request.internalId, ThresholdNextThirtyDaysId.toString)
              }
            } else {
              sessionService.removeEntry(request.internalId, VATRegistrationExceptionId.toString).flatMap {
                _ => sessionService.removeEntry(request.internalId, ThresholdPreviousThirtyDaysId.toString)
              }
            }
          }.flatMap(cacheMap =>
            trafficManagementService.upsertRegistrationInformation(request.internalId, request.currentProfile.registrationID, isOtrs = false).map {
              case RegistrationInformation(_, _, _, _, _) =>
                Redirect(navigator.nextPage(ThresholdInTwelveMonthsId, NormalMode)(new UserAnswers(cacheMap)))
            }
          ))
  }
}
