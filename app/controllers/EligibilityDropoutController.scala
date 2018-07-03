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

package controllers

import config.FrontendAppConfig
import controllers.actions._
import identifiers._
import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.{agriculturalDropout, eligibilityDropout, internationalActivityDropout}

class EligibilityDropoutController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         identify: CacheIdentifierAction) extends FrontendController with I18nSupport {

  def onPageLoad(mode: String) = identify {
    implicit request =>
      mode match {
        case ApplicantUKNinoId.toString => Ok(eligibilityDropout(appConfig, default = false))
        case InternationalActivitiesId.toString => Ok(internationalActivityDropout(appConfig))
        case AgriculturalFlatRateSchemeId.toString => Ok(agriculturalDropout(appConfig))
        case _ => Ok(eligibilityDropout(appConfig))
      }
  }

  def onSubmit: Action[AnyContent] = Action { implicit request =>
    Redirect(controllers.routes.EligibilityDropoutController.onPageLoad(""))
  }
}