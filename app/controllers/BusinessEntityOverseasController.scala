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
import forms.BusinessEntityOverseasFormProvider
import identifiers.{BusinessEntityId, BusinessEntityOverseasId}
import models.{BusinessEntity, NormalMode, OverseasType}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.{Navigator, UserAnswers}
import views.html.BusinessEntityOverseas

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessEntityOverseasController @Inject()(mcc: MessagesControllerComponents,
                                                 sessionService: SessionService,
                                                 navigator: Navigator,
                                                 identify: CacheIdentifierAction,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 formProvider: BusinessEntityOverseasFormProvider,
                                                 view: BusinessEntityOverseas
                                                )(implicit appConfig: FrontendAppConfig, executionContext: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.businessEntity match {
        case Some(overseasEntity: OverseasType) => formProvider().fill(overseasEntity)
        case _ => formProvider()
      }

      Ok(view(preparedForm, routes.BusinessEntityOverseasController.onSubmit()))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      formProvider().bindFromRequest.fold(
        formWithErrors =>
          Future.successful(
            BadRequest(view(formWithErrors, routes.BusinessEntityOverseasController.onSubmit()))
          ),
        entityType => {
          sessionService.save[BusinessEntity](request.internalId, BusinessEntityId.toString, entityType) map { cacheMap =>
            Redirect(navigator.nextPage(BusinessEntityOverseasId, NormalMode)(new UserAnswers(cacheMap)))
          }
        }
      )
  }
}
