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

package featureswitch.core.models

trait FeatureSwitch {
  val configName: String
  val displayName: String
}

object FeatureSwitch {

  case object SubmitDeadlinePage extends FeatureSwitch {
    val configName: String = "feature-switch.submit-deadline-page"
    val displayName: String = "Taxable Turnover May-June Submit Deadline Page"
  }

  case object DeleteAllInvalidTimestampData extends FeatureSwitch {
    val configName: String = "feature-switch.delete-ALL-invalid-timestamp-data"
    val displayName: String = "Enable to delete ALL database items with a String 'lastUpdated' timestamp on start up"
  }

  case object DeleteSomeInvalidTimestampData extends FeatureSwitch {
    val configName: String = "feature-switch.delete-SOME-invalid-timestamp-data"
    val displayName: String = "Enable to delete database items with a String 'lastUpdated' timestamp on start up, up to the config limit"
  }

}
