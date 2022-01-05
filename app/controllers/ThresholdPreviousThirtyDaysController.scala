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
import forms.ThresholdPreviousThirtyDaysFormProvider
import identifiers.{ThresholdPreviousThirtyDaysId, VoluntaryRegistrationId}

import javax.inject.{Inject, Singleton}
import models.{ConditionalDateFormElement, NormalMode}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.{Navigator, ThresholdHelper, UserAnswers}
import views.html.thresholdPreviousThirtyDays

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ThresholdPreviousThirtyDaysController @Inject()(mcc: MessagesControllerComponents,
                                                      sessionService: SessionService,
                                                      navigator: Navigator,
                                                      identify: CacheIdentifierAction,
                                                      getData: DataRetrievalAction,
                                                      requireData: DataRequiredAction,
                                                      formProvider: ThresholdPreviousThirtyDaysFormProvider,
                                                      view: thresholdPreviousThirtyDays
                                                     )(implicit appConfig: FrontendAppConfig, executionContext: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.thresholdPreviousThirtyDays match {
        case None => formProvider()
        case Some(value) => formProvider().fill(value)
      }
      Ok(view(preparedForm, NormalMode))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      formProvider().bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          Future.successful(BadRequest(view(formWithErrors, NormalMode)))
        },
        formValue =>
          sessionService.save[ConditionalDateFormElement](request.internalId, ThresholdPreviousThirtyDaysId.toString, formValue).flatMap {
            cacheMap =>
              val userAnswers = new UserAnswers(cacheMap)
              if (ThresholdHelper.q1DefinedAndTrue(userAnswers) | formValue.value | userAnswers.thresholdNextThirtyDays.fold(false)(_.value)) {
                sessionService.removeEntry(request.internalId, VoluntaryRegistrationId.toString)
              } else {
                Future.successful(cacheMap)
              }
          }.map(cacheMap => Redirect(navigator.nextPage(ThresholdPreviousThirtyDaysId, NormalMode)(new UserAnswers(cacheMap)))))
  }
}