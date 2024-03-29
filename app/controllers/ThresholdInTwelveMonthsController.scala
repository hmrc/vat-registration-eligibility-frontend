/*
 * Copyright 2024 HM Revenue & Customs
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
import forms.ThresholdInTwelveMonthsFormProvider
import identifiers._
import models.{ConditionalDateFormElement, NETP, NormalMode, Overseas}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{SessionService, ThresholdService}
import utils.{Navigator, UserAnswers}
import views.html.ThresholdInTwelveMonths

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ThresholdInTwelveMonthsController @Inject()(sessionService: SessionService,
                                                  navigator: Navigator,
                                                  identify: CacheIdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  formProvider: ThresholdInTwelveMonthsFormProvider,
                                                  view: ThresholdInTwelveMonths)
                                                 (implicit appConfig: FrontendAppConfig,
                                                  mcc: MessagesControllerComponents,
                                                  executionContext: ExecutionContext) extends BaseController with ThresholdService {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      (request.userAnswers.fixedEstablishment, request.userAnswers.registeringBusiness, request.userAnswers.businessEntity) match {
        case (Some(true), Some(_), _) =>
          val preparedForm = request.userAnswers.thresholdInTwelveMonths match {
            case None => formProvider()
            case Some(value) => formProvider().fill(value)
          }
          Ok(view(preparedForm, NormalMode, request.userAnswers.isPartnership, formattedVatThreshold()))
        case (Some(false), Some(_), Some(NETP | Overseas)) =>
          Redirect(navigator.pageIdToPageLoad(TaxableSuppliesInUkId))
        case _ => Redirect(controllers.routes.FixedEstablishmentController.onPageLoad)
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      formProvider().bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, NormalMode, vatThreshold = formattedVatThreshold()))),
        formValue =>
          sessionService.save[ConditionalDateFormElement](ThresholdInTwelveMonthsId.toString, formValue).flatMap { _ =>
            if (formValue.value) {
              sessionService.removeEntry(VoluntaryRegistrationId.toString).flatMap {
                _ => sessionService.removeEntry(ThresholdNextThirtyDaysId.toString)
              }
            } else {
              sessionService.removeEntry(VATRegistrationExceptionId.toString).flatMap {
                _ => sessionService.removeEntry(ThresholdPreviousThirtyDaysId.toString)
              }
            }
          }.map(cacheMap =>
            Redirect(navigator.nextPage(ThresholdInTwelveMonthsId, NormalMode)(request)(new UserAnswers(cacheMap)))
          ))
  }
}
