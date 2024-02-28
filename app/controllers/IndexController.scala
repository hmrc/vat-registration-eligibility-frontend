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
import controllers.actions.{CacheIdentifierAction, DataRetrievalAction}
import identifiers.Identifier
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.JourneyService
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import utils.Navigator

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class IndexController @Inject()(val authConnector: AuthConnector,
                                navigator: Navigator,
                                identify: CacheIdentifierAction,
                                getData: DataRetrievalAction,
                                journeyService: JourneyService)
                               (implicit executionContext: ExecutionContext,
                                mcc: MessagesControllerComponents,
                                appConfig: FrontendAppConfig) extends BaseController with AuthorisedFunctions {

  def onPageLoad: Action[AnyContent] = (identify andThen getData) { _ =>
    Redirect(controllers.routes.FixedEstablishmentController.onPageLoad)
  }

  def initJourney(regId: String): Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      journeyService.initialiseJourney(regId).map { _ =>
        Redirect(controllers.routes.FixedEstablishmentController.onPageLoad)
      }
    }.handleErrorResult
  }

  def navigateToPageId(pageId: String, regId: String): Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      journeyService.initialiseJourney(regId).map { _ =>
        Redirect(navigator.pageIdToPageLoad(new Identifier {
          override def toString: String = pageId
        }))
      }
    }.handleErrorResult
  }
}
