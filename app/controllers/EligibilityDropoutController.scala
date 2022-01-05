/*
 * Copyright 2022 HM Revenue & Customs
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
import models.RegistrationInformation
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.TrafficManagementService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.{vatDivisionDropout, _}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class EligibilityDropoutController @Inject()(mcc: MessagesControllerComponents,
                                             identify: CacheIdentifierAction,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             trafficManagementService: TrafficManagementService,
                                             internationalActivityDropoutView: internationalActivityDropout,
                                             agriculturalDropoutView: agriculturalDropout,
                                             vatDivisionDropoutView: vatDivisionDropout
                                            )(implicit appConfig: FrontendAppConfig,
                                              executionContext: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(mode: String): Action[AnyContent] = identify {
    implicit request =>
      mode match {
        case AgriculturalFlatRateSchemeId.toString => Ok(agriculturalDropoutView())
        case VATExceptionKickoutId.toString => SeeOther(appConfig.VATWriteInURL)
        case BusinessEntityId.toString => Ok(vatDivisionDropoutView())
        case _ => SeeOther(appConfig.otrsUrl)
      }
  }

  def internationalActivitiesDropout: Action[AnyContent] = identify {
    implicit request =>
      Ok(internationalActivityDropoutView())
  }

  def onSubmit: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    trafficManagementService.upsertRegistrationInformation(request.internalId, request.currentProfile.registrationID, isOtrs = true).map {
      case RegistrationInformation(_, _, _, _, _) =>
        Redirect(controllers.routes.EligibilityDropoutController.onPageLoad(""))
    }
  }
}
