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

package common.enums

import scala.language.implicitConversions

object EligibilityQuestions extends Enumeration {
  val haveNino                = Value
  val doingBusinessAbroad     = Value
  val doAnyApplyToYou         = Value
  val applyingForAnyOf        = Value
  val applyingForVatExemption = Value
  val companyWillDoAnyOf      = Value

  implicit def eligibilityQuestionValueToString(value: EligibilityQuestions.Value): String = value.toString
}
