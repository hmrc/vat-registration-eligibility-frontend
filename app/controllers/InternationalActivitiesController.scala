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
import forms.InternationalActivitiesFormProvider
import identifiers.InternationalActivitiesId
import models.NormalMode
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import utils.{Navigator, UserAnswers}
import views.html.InternationalActivities

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class InternationalActivitiesController @Inject()(sessionService: SessionService,
                                                  navigator: Navigator,
                                                  identify: CacheIdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  formProvider: InternationalActivitiesFormProvider,
                                                  view: InternationalActivities)
                                                 (implicit appConfig: FrontendAppConfig,
                                                  mcc: MessagesControllerComponents,
                                                  executionContext: ExecutionContext) extends BaseController {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.internationalActivities match {
        case None => formProvider()
        case Some(value) => formProvider().fill(value)
      }
      Ok(view(preparedForm, NormalMode, request.userAnswers.isPartnership))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      formProvider().bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, NormalMode))),
        value =>
          sessionService.save[Boolean](InternationalActivitiesId.toString, value).map(cacheMap =>
            Redirect(navigator.nextPage(InternationalActivitiesId, NormalMode)(request)(new UserAnswers(cacheMap))))
      )
  }
}
