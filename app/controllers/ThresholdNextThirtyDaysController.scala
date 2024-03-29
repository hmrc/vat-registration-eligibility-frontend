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
import forms.ThresholdNextThirtyDaysFormProvider
import identifiers.{ThresholdNextThirtyDaysId, VoluntaryRegistrationId}
import models.{ConditionalDateFormElement, NormalMode}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{SessionService, ThresholdService}
import utils.{Navigator, UserAnswers}
import views.html.ThresholdNextThirtyDays

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ThresholdNextThirtyDaysController @Inject()(sessionService: SessionService,
                                                  navigator: Navigator,
                                                  identify: CacheIdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  formProvider: ThresholdNextThirtyDaysFormProvider,
                                                  view: ThresholdNextThirtyDays)
                                                 (implicit appConfig: FrontendAppConfig,
                                                  mcc: MessagesControllerComponents,
                                                  executionContext: ExecutionContext) extends BaseController with ThresholdService {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.thresholdNextThirtyDays match {
        case None => formProvider(formattedVatThreshold())
        case Some(value) => formProvider(formattedVatThreshold()).fill(value)
      }
      Ok(view(preparedForm, NormalMode, request.userAnswers.isPartnership, vatThreshold = formattedVatThreshold()))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      formProvider(formattedVatThreshold()).bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, NormalMode, vatThreshold = formattedVatThreshold()))),
        formValue =>
          sessionService.save[ConditionalDateFormElement](ThresholdNextThirtyDaysId.toString, formValue).flatMap {
            cacheMap =>
              if (formValue.value) {
                sessionService.removeEntry(VoluntaryRegistrationId.toString)
              } else {
                Future.successful(cacheMap)
              }
          }.map(cMap => Redirect(navigator.nextPage(ThresholdNextThirtyDaysId, NormalMode)(request)(new UserAnswers(cMap)))))
  }
}