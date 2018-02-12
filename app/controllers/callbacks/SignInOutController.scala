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

package controllers.callbacks

import javax.inject.Inject

import config.AuthClientConnector
import controllers.VatRegistrationController
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.CurrentProfileService
import uk.gov.hmrc.play.config.inject.ServicesConfig
import utils.SessionProfile

import scala.concurrent.Future

class SignInOutControllerImpl @Inject()(val messagesApi: MessagesApi,
                                        val authConnector: AuthClientConnector,
                                        val currentProfileService: CurrentProfileService,
                                        config: ServicesConfig) extends SignInOutController {
  lazy val compRegFEURL = config.getConfString("company-registration-frontend.www.url", "")
  lazy val compRegFEURI = config.getConfString("company-registration-frontend.www.uri", "")
}

trait SignInOutController extends VatRegistrationController with SessionProfile {
  val compRegFEURL: String
  val compRegFEURI: String

  def postSignIn: Action[AnyContent] = isAuthenticated {
    implicit request =>
      Future.successful(Redirect(s"$compRegFEURL$compRegFEURI/post-sign-in"))
  }

  def signOut: Action[AnyContent] = isAuthenticated {
    implicit request =>
      Future.successful(Redirect(s"$compRegFEURL$compRegFEURI/questionnaire").withNewSession)
  }
}



