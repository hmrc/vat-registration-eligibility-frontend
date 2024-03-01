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
import controllers.actions.{CacheIdentifierAction, DataRequiredAction, DataRetrievalAction}
import featureswitch.core.config.FeatureSwitching
import forms.FixedEstablishmentFormProvider
import identifiers.FixedEstablishmentId
import models.NormalMode
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{DataCleardownService, SessionService}
import utils.{Navigator, UserAnswers}
import views.html.FixedEstablishment

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FixedEstablishmentController @Inject()(sessionService: SessionService,
                                             navigator: Navigator,
                                             identify: CacheIdentifierAction,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             formProvider: FixedEstablishmentFormProvider,
                                             view: FixedEstablishment,
                                             dataCleardownService: DataCleardownService)
                                            (implicit appConfig: FrontendAppConfig,
                                             mcc: MessagesControllerComponents,
                                             executionContext: ExecutionContext) extends BaseController with FeatureSwitching {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.fixedEstablishment match {
        case None => formProvider()
        case Some(value) => formProvider().fill(value)
      }
      Ok(view(preparedForm))

  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      formProvider().bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors))),
        value =>
          for {
            _ <- dataCleardownService.cleardownOnAnswerChange(value, FixedEstablishmentId)
            cacheMap <- sessionService.save[Boolean](FixedEstablishmentId.toString, value)
            redirect = Redirect(navigator.nextPage(FixedEstablishmentId, NormalMode)(request)(new UserAnswers(cacheMap)))
          } yield redirect
      )
  }

}
