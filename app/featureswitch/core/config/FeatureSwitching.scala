/*
 * Copyright 2024 HM Revenue & Customs
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

import config.FrontendAppConfig
import featureswitch.core.models.FeatureSwitch
import play.api.Logging
import play.api.mvc.Request
import utils.LoggingUtil

import scala.sys.SystemProperties

trait FeatureSwitching extends LoggingUtil {

  lazy val featureSwitchingModule: FeatureSwitchingModule = new FeatureSwitchingModule()

  val FEATURE_SWITCH_ON = "true"
  val FEATURE_SWITCH_OFF = "false"

  def getValue(key: String)(implicit appConfig: FrontendAppConfig): String = {
    sys.props.get(key).getOrElse(appConfig.servicesConfig.getString(key))
  }

  def getValue(featureSwitch: FeatureSwitch)(implicit appConfig: FrontendAppConfig): String = {
    getValue(featureSwitch.configName)
  }

  def isEnabled(featureSwitch: FeatureSwitch)(implicit appConfig: FrontendAppConfig): Boolean = {
    getValue(featureSwitch).toBoolean
  }

  def isDisabled(featureSwitch: FeatureSwitch)(implicit appConfig: FrontendAppConfig): Boolean = {
    !getValue(featureSwitch).toBoolean
  }

  def setValue(key: String, value: String): SystemProperties = {
    sys.props += key -> value
  }

  def setValue(featureSwitch: FeatureSwitch, value: String): SystemProperties = {
    setValue(featureSwitch.configName, value)
  }

  def resetValue(key: String): SystemProperties = {
    sys.props -= key
  }

  def resetValue(featureSwitch: FeatureSwitch): SystemProperties = {
    resetValue(featureSwitch.configName)
  }

  def enable(featureSwitch: FeatureSwitch)(implicit request: Request[_]): SystemProperties = {
    infoLog(s"[FeatureToggleSupport][enable] ${featureSwitch.configName} enabled")
    setValue(featureSwitch, true.toString)
  }

  def disable(featureSwitch: FeatureSwitch)(implicit request: Request[_]): SystemProperties = {
    debugLog(s"[FeatureToggleSupport][disable] ${featureSwitch.configName} disabled")
    setValue(featureSwitch, false.toString)
  }

  def resetAll(): Unit = {
    featureSwitchingModule.switches.foreach(resetValue)
  }

}
