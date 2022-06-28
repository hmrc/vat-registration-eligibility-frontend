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

package featureswitch.core.config

import javax.inject.Singleton
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import featureswitch.core.models.FeatureSwitch

@Singleton
class FeatureSwitchingModule extends Module with FeatureSwitchRegistry {

  val switches = Seq(
    TrafficManagement,
    SoleTraderFlow,
    PartnershipFlow,
    RegisteredSocietyFlow,
    NonIncorpTrustFlow,
    CharityFlow,
    UnincorporatedAssociationFlow,
    NETPFlow,
    NonUkCompanyFlow,
    ThirdPartyTransactorFlow,
    IndividualFlow,
    VATGroupFlow,
    LandAndProperty,
    TOGCFlow,
    OBIFlow
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

case object SoleTraderFlow extends FeatureSwitch {
  override val configName: String = "feature-switch.soletrader-flow"
  override val displayName: String = "Enable Sole Trader flow"
}

case object PartnershipFlow extends FeatureSwitch {
  override val configName: String = "feature-switch.partnership-flow"
  override val displayName: String = "Enable Partnership flow"
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

case object ThirdPartyTransactorFlow extends FeatureSwitch {
  override val configName: String = "feature-switch.enable-third-party-transactor"
  override val displayName: String = "Enable Third Party Transactor flow"
}

case object IndividualFlow extends FeatureSwitch {
  override val configName: String = "feature-switch.use-individual-flow"
  override val displayName: String = "Use Individual flow"
}

case object VATGroupFlow extends FeatureSwitch {
  override val configName: String = "feature-switch.vat-group-flow"
  override val displayName: String = "Enable VAT Group flow"
}

case object LandAndProperty extends FeatureSwitch {
  override val configName: String = "feature-switch.land-and-property"
  override val displayName: String = "Enable land and property (hide racehorses page)"
}

case object TOGCFlow extends FeatureSwitch {
  override val configName: String = "feature-switch.togc-cole-flow"
  override val displayName: String = "Enable Transfer of Going Concern (TOGC)"
}

case object OBIFlow extends FeatureSwitch {
  override val configName: String = "feature-switch.other-business-involvement-flow"
  override val displayName: String = "Enable Other Business Involvement (OBI) flow"
}
