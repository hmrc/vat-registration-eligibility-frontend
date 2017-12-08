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

package controllers.test

import javax.inject.Inject

import connectors.S4LConnector
import controllers.VatRegistrationController
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.CurrentProfileService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.SessionProfile

class TestCacheControllerImpl @Inject()(val messagesApi: MessagesApi,
                                        val authConnector: AuthConnector,
                                        val currentProfileService: CurrentProfileService,
                                        val s4lConnector: S4LConnector) extends TestCacheController

trait TestCacheController extends VatRegistrationController with SessionProfile {
  val s4lConnector: S4LConnector

  def tearDownS4L: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { profile =>
          s4lConnector.clear(profile.registrationId).map {
            _ => Ok("Eligibility Frontend Save4Later cleared")
          }
        }
  }
}
