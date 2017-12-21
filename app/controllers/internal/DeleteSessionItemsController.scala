/*
 * Copyright 2017 HM Revenue & Customs
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

package controllers.internal

import javax.inject.Inject

import controllers.VatRegistrationController
import org.slf4j.{Logger, LoggerFactory}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import services.CancellationService
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class DeleteSessionItemsControllerImpl @Inject()(val messagesApi: MessagesApi,
                                                 val authConnector: AuthConnector,
                                                 val cancelService: CancellationService) extends DeleteSessionItemsController

trait DeleteSessionItemsController extends VatRegistrationController {
  val cancelService: CancellationService

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  def deleteSessionRelatedData(regId: String): Action[AnyContent] = Action.async {
    implicit request =>
      implicit val hc = HeaderCarrierConverter.fromHeadersAndSession(request.headers)
      authConnector.currentAuthority flatMap {
        _.fold(unauthorised){ _ =>
          cancelService.deleteEligibilityData(regId) map (if(_) Ok else BadRequest)
        }
      } recover {
        case ex: Exception =>
          logger.error(s"[deleteSessionRelatedData] - received and error on receiving the authority - error: ${ex.getMessage}")
          InternalServerError
      }
  }

  private def unauthorised: Future[Result] = {
    logger.warn(s"[deleteSessionRelatedData] - Can't get Authority")
    Future.successful(Unauthorized)
  }
}
