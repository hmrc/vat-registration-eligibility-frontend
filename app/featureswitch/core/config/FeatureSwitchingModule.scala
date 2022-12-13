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

import featureswitch.core.models.FeatureSwitch
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}

import javax.inject.Singleton

@Singleton
class FeatureSwitchingModule extends Module with FeatureSwitchRegistry {

  val switches = Seq(
    SoleTraderFlow,
    PartnershipFlow,
    ThirdPartyTransactorFlow,
    IndividualFlow,
    LandAndProperty,
    WelshLanguage
  )

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(
      bind[FeatureSwitchRegistry].to(this).eagerly()
    )
  }
}

case object SoleTraderFlow extends FeatureSwitch {
  override val configName: String = "feature-switch.soletrader-flow"
  override val displayName: String = "Enable Sole Trader flow"
}

case object PartnershipFlow extends FeatureSwitch {
  override val configName: String = "feature-switch.partnership-flow"
  override val displayName: String = "Enable Partnership flow"
}

case object NETPFlow extends FeatureSwitch {
  override val configName: String = "feature-switch.enable-netp"
  override val displayName: String = "Enable non-established taxable person (NETP) flow"
}

case object ThirdPartyTransactorFlow extends FeatureSwitch {
  override val configName: String = "feature-switch.enable-third-party-transactor"
  override val displayName: String = "Enable Third Party Transactor flow"
}

case object IndividualFlow extends FeatureSwitch {
  override val configName: String = "feature-switch.use-individual-flow"
  override val displayName: String = "Use Individual flow"
}

case object LandAndProperty extends FeatureSwitch {
  override val configName: String = "feature-switch.land-and-property"
  override val displayName: String = "Enable land and property (hide racehorses page)"
}

case object WelshLanguage extends FeatureSwitch {
  override val configName: String = "feature-switch.welsh"
  override val displayName: String = "Enable Welsh Language"
}
