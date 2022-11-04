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
import forms.DateOfBusinessTransferFormProvider
import identifiers._
import models.{DateFormElement, NormalMode}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import utils.{Navigator, UserAnswers}
import views.html.DateOfBusinessTransfer

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DateOfBusinessTransferController @Inject()(sessionService: SessionService,
                                                 navigator: Navigator,
                                                 identify: CacheIdentifierAction,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 formProvider: DateOfBusinessTransferFormProvider,
                                                 view: DateOfBusinessTransfer)
                                                (implicit appConfig: FrontendAppConfig,
                                                 mcc: MessagesControllerComponents,
                                                 executionContext: ExecutionContext) extends BaseController {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request => {
      val preparedForm = request.userAnswers.dateOfBusinessTransfer match {
        case None => formProvider(request.userAnswers.togcColeKey)
        case Some(value) => formProvider(request.userAnswers.togcColeKey).fill(value)
      }
      Ok(view(preparedForm, NormalMode, request.userAnswers.togcColeKey))
    }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      formProvider(request.userAnswers.togcColeKey).bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, NormalMode, request.userAnswers.togcColeKey))),
        formValue =>
          sessionService.save[DateFormElement](DateOfBusinessTransferId.toString, formValue).flatMap {
            cacheMap => Future.successful(cacheMap)
          }.map(cMap => Redirect(navigator.nextPage(DateOfBusinessTransferId, NormalMode)(new UserAnswers(cMap)))))
  }
}
