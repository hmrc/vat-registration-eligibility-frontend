/*
 * Copyright 2023 HM Revenue & Customs
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
import forms.ThresholdPreviousThirtyDaysFormProvider
import identifiers.{ThresholdPreviousThirtyDaysId, VoluntaryRegistrationId}
import models.{ConditionalDateFormElement, NormalMode}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import utils.{Navigator, UserAnswers}
import views.html.ThresholdPreviousThirtyDays

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ThresholdPreviousThirtyDaysController @Inject()(sessionService: SessionService,
                                                      navigator: Navigator,
                                                      identify: CacheIdentifierAction,
                                                      getData: DataRetrievalAction,
                                                      requireData: DataRequiredAction,
                                                      formProvider: ThresholdPreviousThirtyDaysFormProvider,
                                                      view: ThresholdPreviousThirtyDays)
                                                     (implicit appConfig: FrontendAppConfig,
                                                      mcc: MessagesControllerComponents,
                                                      executionContext: ExecutionContext) extends BaseController {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.thresholdPreviousThirtyDays match {
        case None => formProvider()
        case Some(value) => formProvider().fill(value)
      }
      Ok(view(preparedForm, NormalMode, request.userAnswers.isPartnership))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      formProvider().bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          Future.successful(BadRequest(view(formWithErrors, NormalMode)))
        },
        formValue =>
          sessionService.save[ConditionalDateFormElement](ThresholdPreviousThirtyDaysId.toString, formValue).flatMap {
            cacheMap =>
              val userAnswers = new UserAnswers(cacheMap)
              if (userAnswers.thresholdInTwelveMonths.exists(_.value) | formValue.value | userAnswers.thresholdNextThirtyDays.exists(_.value)) {
                sessionService.removeEntry(VoluntaryRegistrationId.toString)
              } else {
                Future.successful(cacheMap)
              }
          }.map(cacheMap => Redirect(navigator.nextPage(ThresholdPreviousThirtyDaysId, NormalMode)(request)(new UserAnswers(cacheMap)))))
  }
}