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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}

import java.net.URLEncoder
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FeedbackController @Inject()(implicit mcc: MessagesControllerComponents,
                                   appConfig: FrontendAppConfig,
                                   executionContext: ExecutionContext) extends BaseController {

  lazy val betaFeedbackUrl: String = appConfig.betaFeedbackUrl

  def contactFormReferrer(implicit request: Request[AnyContent]): String = request.headers.get(REFERER).getOrElse("")

  def show: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Redirect(feedbackFormPartialUrl))
  }

  private def feedbackFormPartialUrl(implicit request: Request[AnyContent]) =
    s"$betaFeedbackUrl&backUrl=${urlEncode(contactFormReferrer)}"

  private def urlEncode(value: String) = URLEncoder.encode(value, "UTF-8")

}
