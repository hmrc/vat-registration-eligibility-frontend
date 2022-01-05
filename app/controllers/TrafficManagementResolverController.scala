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
import connectors.{Allocated, QuotaReached}
import controllers.actions.{CacheIdentifierAction, DataRequiredAction, DataRetrievalAction}
import featureswitch.core.config.{FeatureSwitching, TrafficManagement}
import identifiers.TrafficManagementResolverId
import models.{Draft, NormalMode, RegistrationInformation, VatReg}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{S4LService, TrafficManagementService}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.{Navigator, UserAnswers}

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TrafficManagementResolverController @Inject()(mcc: MessagesControllerComponents,
                                                    s4LService: S4LService,
                                                    navigator: Navigator,
                                                    identify: CacheIdentifierAction,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    trafficManagementService: TrafficManagementService
                                                   )(implicit appConfig: FrontendAppConfig,
                                                     executionContext: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with FeatureSwitching {

  def resolve: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      def redirectToNextPage: Result = Redirect(navigator.nextPage(TrafficManagementResolverId, NormalMode)(new UserAnswers(request.userAnswers.cacheMap)))

      if (isEnabled(TrafficManagement)) {
        trafficManagementService.getRegistrationInformation(request.currentProfile.registrationID).flatMap {
          case Some(RegistrationInformation(_, _, Draft, Some(date), VatReg)) if date == LocalDate.now =>
            Future.successful(redirectToNextPage)
          case _ =>
            trafficManagementService.allocate(
              request.currentProfile.registrationID,
              request.userAnswers.businessEntity.getOrElse(throw new InternalServerException("[NinoController] Missing business entity"))
            ).flatMap {
              case Allocated =>
                s4LService.save[CacheMap](request.currentProfile.registrationID, "eligibility-data", request.userAnswers.cacheMap) map { _ =>
                  redirectToNextPage
                }
              case QuotaReached =>
                if (request.userAnswers.fixedEstablishment.contains(true)) {
                  Future.successful(Redirect(controllers.routes.VATExceptionKickoutController.onPageLoad))
                } else {
                  Future.successful(Redirect(appConfig.otrsUrl))
                }
            }
        }
      }
      else {
        trafficManagementService.upsertRegistrationInformation(
          internalId = request.internalId,
          regId = request.currentProfile.registrationID,
          isOtrs = false
        ).map(_ => redirectToNextPage)
      }
  }
}
