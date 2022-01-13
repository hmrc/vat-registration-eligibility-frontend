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
import forms.BusinessEntityOtherFormProvider
import identifiers.{BusinessEntityId, BusinessEntityOtherId}
import models._
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.{Navigator, UserAnswers}
import views.html.businessEntityOther

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessEntityOtherController @Inject()(mcc: MessagesControllerComponents,
                                              sessionService: SessionService,
                                              navigator: Navigator,
                                              identify: CacheIdentifierAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              formProvider: BusinessEntityOtherFormProvider,
                                              view: businessEntityOther
                                             )(implicit appConfig: FrontendAppConfig, executionContext: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.businessEntity match {
        case Some(businessEntity: OtherType) => formProvider().fill(businessEntity)
        case _ => formProvider()
      }
      Ok(view(preparedForm, controllers.routes.BusinessEntityOtherController.onSubmit()))
  }

  def onSubmit: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    formProvider().bindFromRequest.fold(
      formWithErrors =>
        Future.successful(
          BadRequest(view(formWithErrors, routes.BusinessEntityOtherController.onSubmit()))
        ),
      entityType => {
        sessionService.save[BusinessEntity](BusinessEntityId.toString, entityType) map { cacheMap =>
          Redirect(navigator.nextPage(BusinessEntityOtherId, NormalMode)(new UserAnswers(cacheMap)))
        }
      }
    )
  }
}