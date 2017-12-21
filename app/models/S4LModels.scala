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

package models

import models.view._
import play.api.libs.json.{Json, OFormat}

case class S4LVatEligibility(haveNino: Option[Boolean] = None,
                             doingBusinessAbroad: Option[Boolean] = None,
                             doAnyApplyToYou: Option[Boolean] = None,
                             applyingForAnyOf: Option[Boolean] = None,
                             applyingForVatExemption: Option[Boolean] = None,
                             companyWillDoAnyOf: Option[Boolean] = None)

object S4LVatEligibility {
  implicit val format: OFormat[S4LVatEligibility] = Json.format[S4LVatEligibility]
}

case class S4LVatEligibilityChoice(taxableTurnover: Option[TaxableTurnover] = None,
                                   voluntaryRegistration: Option[VoluntaryRegistration] = None,
                                   voluntaryRegistrationReason: Option[VoluntaryRegistrationReason] = None,
                                   overThreshold: Option[OverThresholdView] = None,
                                   expectationOverThreshold: Option[ExpectationOverThresholdView] = None)

object S4LVatEligibilityChoice {
  implicit val format: OFormat[S4LVatEligibilityChoice] = Json.format[S4LVatEligibilityChoice]
}