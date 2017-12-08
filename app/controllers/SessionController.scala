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

package controllers

import java.io.File
import javax.inject.Inject

import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import views.html.pages.error.TimeoutView

import scala.concurrent.Future

class SessionControllerImpl @Inject()(implicit val messagesApi: MessagesApi,
                                      val authConnector: AuthConnector) extends SessionController

trait SessionController extends VatRegistrationController {

  def renewSession: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        Future.successful(Ok.sendFile(new File("conf/renewSession.jpg")).as("image/jpeg"))
  }

  def destroySession: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Redirect(routes.SessionController.timeoutShow()).withNewSession)
  }

  def timeoutShow = Action.async {
    implicit request =>
      Future.successful(Ok(TimeoutView()))
  }
}
