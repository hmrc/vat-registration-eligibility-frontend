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
import forms.BusinessEntityFormProvider
import identifiers.{BusinessEntityId, BusinessEntityOverseasId}
import models._
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import utils.{Navigator, UserAnswers}
import views.html.BusinessEntityView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessEntityController @Inject()(sessionService: SessionService,
                                         navigator: Navigator,
                                         identify: CacheIdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: BusinessEntityFormProvider,
                                         view: BusinessEntityView)
                                        (implicit appConfig: FrontendAppConfig,
                                         executionContext: ExecutionContext,
                                         mcc: MessagesControllerComponents) extends BaseController {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      request.userAnswers.businessEntity match {
        case Some(_: OverseasType) if request.userAnswers.fixedEstablishment.contains(false) =>
          Redirect(navigator.pageIdToPageLoad(BusinessEntityOverseasId))
        // This allows the CYA redirect to send the user back to the Overseas Business Entity page, this is necessary
        // because all 4 business entity pages store to the same key on eligibility which is later used by the
        // CYA page to create the change link, doing it any other way would require a lot of extra work.
        case otherTypes =>
          val preparedForm = otherTypes match {
            case Some(_: OtherType) => formProvider().fill(Other)
            case Some(_: PartnershipType) => formProvider().fill(Partnership)
            case None | Some(_: OverseasType) => formProvider()
            case Some(businessEntity) => formProvider().fill(businessEntity)
          }

          Ok(view(preparedForm, controllers.routes.BusinessEntityController.onSubmit()))
      }

  }

  def onSubmit: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    formProvider().bindFromRequest.fold(
      formWithErrors =>
        Future.successful(
          BadRequest(view(formWithErrors, routes.BusinessEntityController.onSubmit()))
        ),
      {
        case entityType =>
          sessionService.save[BusinessEntity](BusinessEntityId.toString, entityType) map { cacheMap =>
            Redirect(navigator.nextPage(BusinessEntityId, NormalMode)(new UserAnswers(cacheMap)))
          }
      }
    )
  }
}