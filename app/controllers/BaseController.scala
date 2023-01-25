/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.actions.VatRegLanguageSupport
import featureswitch.core.config.FeatureSwitching
import play.api.Logging
import play.api.mvc.{MessagesControllerComponents, Result}
import uk.gov.hmrc.auth.core.{AuthorisationException, NoActiveSession}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

abstract class BaseController @Inject()(implicit val mcc: MessagesControllerComponents,
                                        appConfig: FrontendAppConfig,
                                        executionContext: ExecutionContext) extends FrontendController(mcc)
  with VatRegLanguageSupport
  with FeatureSwitching
  with Logging {

  implicit class HandleResult(res: Future[Result])(implicit hc: HeaderCarrier) {
    def handleErrorResult: Future[Result] = {
      res recoverWith {
        case _: NoActiveSession =>
          Future.successful(Redirect(appConfig.loginUrl, Map("continue" -> Seq(appConfig.postSignInUrl))))
        case ae: AuthorisationException =>
          logger.info(s"User is not authorised - reason: ${ae.reason}")
          Future.successful(Redirect(routes.UnauthorisedController.onPageLoad))
        case e =>
          logger.warn(s"An exception occurred - err: ${e.getMessage}")
          throw e
      }
    }
  }
}
