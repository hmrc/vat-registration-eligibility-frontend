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

import controllers.actions.{CacheIdentifierAction, DataRetrievalAction}
import identifiers.Identifier
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.JourneyService
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Navigator

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IndexController @Inject()(val authConnector: AuthConnector,
                                mcc: MessagesControllerComponents,
                                navigator: Navigator,
                                identify: CacheIdentifierAction,
                                getData: DataRetrievalAction,
                                journeyService: JourneyService
                               )(implicit executionContext: ExecutionContext) extends FrontendController(mcc) with I18nSupport with AuthorisedFunctions {

  def onPageLoad: Action[AnyContent] = (identify andThen getData) { implicit request =>
    Redirect(controllers.routes.FixedEstablishmentController.onPageLoad)
  }

  def initJourney(regId: String): Action[AnyContent] = Action.async { implicit request =>
    authorised().retrieve(Retrievals.internalId) {
      case Some(internalId) =>
        journeyService.initialiseJourney(internalId, regId).map { _ =>
          Redirect(controllers.routes.FixedEstablishmentController.onPageLoad)
        }
      case _ =>
        Future.successful(Redirect(routes.UnauthorisedController.onPageLoad))
    }
  }

  def navigateToPageId(pageId: String): Action[AnyContent] = Action { _ =>
    Redirect(navigator.pageIdToPageLoad(new Identifier {
      override def toString: String = pageId
    }))
  }

}
