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

package featureswitch.core.config

import javax.inject.Singleton
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import featureswitch.core.models.FeatureSwitch

@Singleton
class FeatureSwitchingModule extends Module with FeatureSwitchRegistry {

  val switches = Seq(
    TrafficManagement,
    EnableAAS,
    SoleTraderFlow,
    GeneralPartnershipFlow,
    RegisteredSocietyFlow,
    NonIncorpTrustFlow,
    CharityFlow,
    UnincorporatedAssociationFlow,
    NETPFlow,
    NonUkCompanyFlow,
    NIPFlow
  )

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(
      bind[FeatureSwitchRegistry].to(this).eagerly()
    )
  }
}

case object TrafficManagement extends FeatureSwitch {
  override val configName: String = "feature-switch.traffic-management"
  override val displayName: String = "Use traffic management"
}

case object EnableAAS extends FeatureSwitch {
  override val configName: String = "feature-switch.enable-aas"
  override val displayName: String = "Enable AAS (only use with the frontend AAS feature switch)"
}

case object SoleTraderFlow extends FeatureSwitch {
  override val configName: String = "feature-switch.soletrader-flow"
  override val displayName: String = "Enable Sole Trader flow"
}

case object GeneralPartnershipFlow extends FeatureSwitch {
  override val configName: String = "feature-switch.general-partnership-flow"
  override val displayName: String = "Enable General Partnership flow"
}

case object RegisteredSocietyFlow extends FeatureSwitch {
  override val configName: String = "feature-switch.registered-society-flow"
  override val displayName: String = "Enable Registered Society flow"
}

case object NonIncorpTrustFlow extends FeatureSwitch {
  override val configName: String = "feature-switch.non-incorp-trust-flow"
  override val displayName: String = "Enable Non-Incorporated Trust flow"
}

case object CharityFlow extends FeatureSwitch {
  override val configName: String = "feature-switch.charity-flow"
  override val displayName: String = "Enable Charitable Incorporated Organisation (CIO) flow"
}

case object UnincorporatedAssociationFlow extends FeatureSwitch {
  override val configName: String = "feature-switch.unincorporated-association"
  override val displayName: String = "Enable Unincorporated Association flow"
}

case object NETPFlow extends FeatureSwitch {
  override val configName: String = "feature-switch.enable-netp"
  override val displayName: String = "Enable non-established taxable person (NETP) flow"
}

case object NonUkCompanyFlow extends FeatureSwitch {
  override val configName: String = "feature-switch.enable-non-uk-company"
  override val displayName: String = "Enable non-uk company (Overseas) flow"
}

case object NIPFlow extends FeatureSwitch {
  override val configName: String = "feature-switch.enable-nip"
  override val displayName: String = "Enable Northern Ireland protocol (NIP) flow"
}
