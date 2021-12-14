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
import controllers.actions.{CacheIdentifierAction, DataRequiredAction, DataRetrievalAction}
import models.RegistrationInformation
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{S4LService, TrafficManagementService, VatRegistrationService}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.eligible

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class EligibleController @Inject()(mcc: MessagesControllerComponents,
                                   identify: CacheIdentifierAction,
                                   getData: DataRetrievalAction,
                                   requireData: DataRequiredAction,
                                   vatRegistrationService: VatRegistrationService,
                                   trafficManagementService: TrafficManagementService,
                                   view: eligible,
                                   s4LService: S4LService
                                  )(implicit appConfig: FrontendAppConfig, executionContext: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  val frontendUrl = s"${appConfig.vatRegFEURL}${appConfig.vatRegFEURI}${appConfig.vatRegFEFirstPage}"

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    Ok(view())
  }

  def onSubmit: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    vatRegistrationService.submitEligibility(hc, implicitly[ExecutionContext], request).flatMap { _ =>
      trafficManagementService.upsertRegistrationInformation(request.internalId, request.regId, isOtrs = false).flatMap {
        case RegistrationInformation(_, _, _, _, _) =>
          s4LService.save[CacheMap](request.regId, "eligibility-data", request.userAnswers.cacheMap).map { _ =>
            Redirect(s"${appConfig.vatRegFEURL}${appConfig.vatRegFEURI}/journey/${request.regId}")
          }
      }
    }
  }

}
