/*
 * Copyright 2021 HM Revenue & Customs
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
import connectors.{Allocated, DataCacheConnector, QuotaReached}
import controllers.actions._
import featureswitch.core.config.{FeatureSwitching, TrafficManagement}
import forms.TaxableSuppliesInUkFormProvider
import identifiers.{EligibilityDropoutId, InternationalActivitiesId, TaxableSuppliesInUkId}
import models.{Draft, NormalMode, RegistrationInformation, VatReg}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{S4LService, TrafficManagementService}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.{Navigator, UserAnswers}
import views.html.TaxableSuppliesInUk

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaxableSuppliesInUkController @Inject()(mcc: MessagesControllerComponents,
                                              dataCacheConnector: DataCacheConnector,
                                              s4LService: S4LService,
                                              navigator: Navigator,
                                              identify: CacheIdentifierAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              formProvider: TaxableSuppliesInUkFormProvider,
                                              trafficManagementService: TrafficManagementService,
                                              view: TaxableSuppliesInUk
                                             )(implicit appConfig: FrontendAppConfig,
                                               executionContext: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with FeatureSwitching {

  val onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.taxableSuppliesInUk match {
        case None => formProvider()
        case Some(value) => formProvider().fill(value)
      }

      Ok(view(preparedForm, NormalMode))
  }

  val onSubmit: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      formProvider().bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, NormalMode))),
        value =>
          dataCacheConnector.save[Boolean](request.internalId, TaxableSuppliesInUkId.toString, value).flatMap { cacheMap =>
            def redirectToNextPage: Result = Redirect(navigator.nextPage(TaxableSuppliesInUkId, NormalMode)(new UserAnswers(cacheMap)))

            (value, isEnabled(TrafficManagement)) match {
              case (false, _) =>
                Future.successful(redirectToNextPage)
              case (true, false) =>
                trafficManagementService.upsertRegistrationInformation(
                  internalId = request.internalId,
                  regId = request.currentProfile.registrationID,
                  isOtrs = false
                ).map(_ => redirectToNextPage)
              case (true, true) =>
                trafficManagementService.getRegistrationInformation.flatMap {
                  case Some(RegistrationInformation(_, _, Draft, Some(date), VatReg)) if date == LocalDate.now =>
                    Future.successful(redirectToNextPage)
                  case _ =>
                    trafficManagementService.allocate(
                      request.currentProfile.registrationID,
                      request.userAnswers.businessEntity.getOrElse(throw new InternalServerException("[TaxableSuppliesInUkController] Missing business entity"))
                    ).flatMap {
                      case Allocated =>
                        s4LService.save[CacheMap](request.currentProfile.registrationID, "eligibility-data", cacheMap) map { _ =>
                          redirectToNextPage
                        }
                      case QuotaReached =>
                        Future.successful(Redirect(appConfig.otrsUrl))
                    }
                }
            }
          }
      )
  }

}
