/*
 * Copyright 2020 HM Revenue & Customs
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

import java.net.URLEncoder

import config.FrontendAppConfig
import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future

class FeedbackController @Inject()(val appConfig: FrontendAppConfig,
                                   val mcc: MessagesControllerComponents) extends FrontendController(mcc) {

  lazy val contactFrontendFeedbackPartialUrl: String = appConfig.contactFrontendFeedbackPartialUrl
  lazy val contactFormServiceIdentifier: String = appConfig.contactFormServiceIdentifier

  def contactFormReferrer(implicit request: Request[AnyContent]): String = request.headers.get(REFERER).getOrElse("")

  def show: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Redirect(feedbackFormPartialUrl))
  }

  private def feedbackFormPartialUrl(implicit request: Request[AnyContent]) =
    s"$contactFrontendFeedbackPartialUrl?backUrl=${urlEncode(contactFormReferrer)}" +
      s"&service=$contactFormServiceIdentifier"

  private def urlEncode(value: String) = URLEncoder.encode(value, "UTF-8")

}
