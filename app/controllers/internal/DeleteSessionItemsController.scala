/*
 * Copyright 2018 HM Revenue & Customs
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

import config.AuthClientConnector
import controllers.VatRegistrationController
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.{CancellationService, CurrentProfileService}
import utils.SessionProfile

class DeleteSessionItemsControllerImpl @Inject()(val messagesApi: MessagesApi,
                                                 val authConnector: AuthClientConnector,
                                                 val cancelService: CancellationService,
                                                 val currentProfileService: CurrentProfileService) extends DeleteSessionItemsController

trait DeleteSessionItemsController extends VatRegistrationController with SessionProfile {
  val cancelService: CancellationService

  def deleteSessionRelatedData(regId: String): Action[AnyContent] = isAuthenticated {
    implicit request =>
      cancelService.deleteEligibilityData(regId) map (if(_) Ok else BadRequest)
  }
}
