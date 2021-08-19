/*
 * Copyright 2021 HM Revenue & Customs
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

import connectors.{DataCacheConnector, S4LConnector}
import controllers.actions.{CacheIdentifierAction, DataRetrievalAction}
import identifiers.Identifier
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Navigator

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class IndexController @Inject()(mcc: MessagesControllerComponents,
                                navigator: Navigator,
                                dataCacheConnector: DataCacheConnector,
                                s4LConnector: S4LConnector,
                                identify: CacheIdentifierAction,
                                getData: DataRetrievalAction
                               )(implicit executionContext: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData).async { implicit request =>
    for {
      _ <- dataCacheConnector.delete(request.internalId) //TODO Remove as part of SAR-6520
      _ <- s4LConnector.clear(request.internalId)
    } yield Redirect(routes.IntroductionController.onPageLoad)
  }

  def navigateToPageId(pageId: String): Action[AnyContent] = Action { _ =>
    Redirect(navigator.pageIdToPageLoad(new Identifier {
      override def toString: String = pageId
    }))
  }

}
