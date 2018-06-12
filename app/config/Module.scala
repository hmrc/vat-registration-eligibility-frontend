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

package config

import com.google.inject.AbstractModule
import connectors._
import controllers.{FeedbackController, FeedbackControllerImpl}
import controllers.actions._
import services.{CurrentProfileService, CurrentProfileServiceImpl, IncorporationInformationService, IncorporationInformationServiceImpl}
import uk.gov.hmrc.play.config.ServicesConfig
import utils.{FeatureManager, FeatureSwitchManager, VATFeatureSwitch, VATFeatureSwitchImpl}

class Module extends AbstractModule {

  override def configure(): Unit = {

    bind(classOf[ServicesConfig]).to(classOf[FrontendAppConfig]).asEagerSingleton()

    // Bind the actions for DI
    bind(classOf[DataRetrievalAction]).to(classOf[DataRetrievalActionImpl]).asEagerSingleton()
    bind(classOf[DataRequiredAction]).to(classOf[DataRequiredActionImpl]).asEagerSingleton()

    bind(classOf[WSHttp]).to(classOf[Http]).asEagerSingleton()

    //controller action binders
    bind(classOf[CacheIdentifierAction]).to(classOf[AuthActionImpl]).asEagerSingleton()

    //controllers
    bind(classOf[FeedbackController]).to(classOf[FeedbackControllerImpl]).asEagerSingleton()

    //connectors
    bind(classOf[IncorporationInformationConnector]).to(classOf[IncorporationInformationConnectorImpl]).asEagerSingleton()
    bind(classOf[BusinessRegistrationConnector]).to(classOf[BusinessRegistrationConnectorImpl]).asEagerSingleton()
    bind(classOf[CompanyRegistrationConnector]).to(classOf[CompanyRegistrationConnectorImpl]).asEagerSingleton()

    //services
    bind(classOf[IncorporationInformationService]).to(classOf[IncorporationInformationServiceImpl]).asEagerSingleton()
    bind(classOf[CurrentProfileService]).to(classOf[CurrentProfileServiceImpl]).asEagerSingleton()

    //feature switch
    bind(classOf[FeatureManager]).to(classOf[FeatureSwitchManager]).asEagerSingleton()
    bind(classOf[VATFeatureSwitch]).to(classOf[VATFeatureSwitchImpl]).asEagerSingleton()
  }
}
