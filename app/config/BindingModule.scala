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

package config

import com.google.inject.AbstractModule
import connectors._
import connectors.test._
import controllers._
import controllers.callbacks._
import controllers.internal._
import controllers.test._
import services._
import uk.gov.hmrc.http.cache.client.{SessionCache, ShortLivedCache, ShortLivedHttpCaching}
import uk.gov.hmrc.play.config.inject.{DefaultServicesConfig, ServicesConfig}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.{FeatureManager, FeatureSwitchManager, VREFEFeatureSwitch, VREFEFeatureSwitches}

class BindingModule extends AbstractModule {
  override def configure(): Unit = {
    bindControllers()
    bindServices()
    bindConnectors()
    bindFeatureSwitches()
    bindOther()
  }

  private def bindControllers(): Unit = {
    bind(classOf[SignInOutController]).to(classOf[SignInOutControllerImpl]).asEagerSingleton()
    bind(classOf[DeleteSessionItemsController]).to(classOf[DeleteSessionItemsControllerImpl]).asEagerSingleton()
    bind(classOf[EligibilityController]).to(classOf[EligibilityControllerImpl]).asEagerSingleton()
    bind(classOf[EligibilitySummaryController]).to(classOf[EligibilitySummaryControllerImpl]).asEagerSingleton()
    bind(classOf[SessionController]).to(classOf[SessionControllerImpl]).asEagerSingleton()
    bind(classOf[TaxableTurnoverController]).to(classOf[TaxableTurnoverControllerImpl]).asEagerSingleton()
    bind(classOf[ThresholdController]).to(classOf[ThresholdControllerImpl]).asEagerSingleton()
    bind(classOf[ThresholdSummaryController]).to(classOf[ThresholdSummaryControllerImpl]).asEagerSingleton()
    bind(classOf[VoluntaryRegistrationController]).to(classOf[VoluntaryRegistrationControllerImpl]).asEagerSingleton()
    bind(classOf[VoluntaryRegistrationReasonController]).to(classOf[VoluntaryRegistrationReasonControllerImpl]).asEagerSingleton()
    bind(classOf[TestCacheController]).to(classOf[TestCacheControllerImpl]).asEagerSingleton()
    bind(classOf[TestSetupController]).to(classOf[TestSetupControllerImpl]).asEagerSingleton()
    bind(classOf[TestVatRegistrationAdminController]).to(classOf[TestVatRegistrationAdminControllerImpl]).asEagerSingleton()
  }

  private def bindServices(): Unit = {
    bind(classOf[CancellationService]).to(classOf[CancellationServiceImpl]).asEagerSingleton()
    bind(classOf[CurrentProfileService]).to(classOf[CurrentProfileServiceImpl]).asEagerSingleton()
    bind(classOf[IncorporationInformationService]).to(classOf[IncorporationInformationServiceImpl]).asEagerSingleton()
    bind(classOf[SummaryService]).to(classOf[SummaryServiceImpl]).asEagerSingleton()
    bind(classOf[VatRegistrationService]).to(classOf[VatRegistrationServiceImpl]).asEagerSingleton()
    bind(classOf[VatRegFrontendService]).to(classOf[VatRegFrontendServiceImpl]).asEagerSingleton()
    bind(classOf[EligibilityService]).to(classOf[EligibilityServiceImpl]).asEagerSingleton()
    bind(classOf[ThresholdService]).to(classOf[ThresholdServiceImpl]).asEagerSingleton()
  }

  private def bindConnectors(): Unit = {
    bind(classOf[TestVatRegistrationConnector]).to(classOf[TestVatRegistrationConnectorImpl]).asEagerSingleton()
    bind(classOf[BusinessRegistrationConnector]).to(classOf[BusinessRegistrationConnectorImpl]).asEagerSingleton()
    bind(classOf[CompanyRegistrationConnector]).to(classOf[CompanyRegistrationConnectorImpl]).asEagerSingleton()
    bind(classOf[IncorporationInformationConnector]).to(classOf[IncorporationInformationConnectorImpl]).asEagerSingleton()
    bind(classOf[KeystoreConnector]).to(classOf[KeystoreConnectorImpl]).asEagerSingleton()
    bind(classOf[S4LConnector]).to(classOf[S4LConnectorImpl]).asEagerSingleton()
    bind(classOf[VatRegistrationConnector]).to(classOf[VatRegistrationConnectorImpl]).asEagerSingleton()
  }

  private def bindFeatureSwitches(): Unit = {
    bind(classOf[FeatureManager]).to(classOf[FeatureSwitchManager]).asEagerSingleton()
    bind(classOf[VREFEFeatureSwitches]).to(classOf[VREFEFeatureSwitch]).asEagerSingleton()
  }

  private def bindOther(): Unit = {
    bind(classOf[ServicesConfig]).to(classOf[DefaultServicesConfig]).asEagerSingleton()
    bind(classOf[WSHttp]).to(classOf[WSHttpImpl]).asEagerSingleton()
    bind(classOf[AuthConnector]).to(classOf[FrontendAuthConnector]).asEagerSingleton()
    bind(classOf[ShortLivedHttpCaching]).to(classOf[VatShortLivedHttpCaching]).asEagerSingleton()
    bind(classOf[ShortLivedCache]).to(classOf[VatShortLivedCache]).asEagerSingleton()
    bind(classOf[SessionCache]).to(classOf[VatSessionCache]).asEagerSingleton()
    bind(classOf[TestS4LBuilder]).to(classOf[TestS4LBuilderImpl]).asEagerSingleton()
  }
}
