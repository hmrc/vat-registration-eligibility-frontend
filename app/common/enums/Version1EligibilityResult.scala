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

import play.api.libs.json.{Format, Reads, Writes}

sealed trait Result extends Enumeration

case class EligibilityResult(version:Int,
                             questionsMap:Map[Int, Result] = Map(1 -> Version1EligibilityResult)
                            ) {
  def questions = version match{
    case 1 => Version1EligibilityResult
  }
}

object Version1EligibilityResult extends Result {
  val noNino = Value
  val doingInternationalBusiness = Value
  val otherInvolvementsOrCOLE = Value
  val wantsAFRSOrAAS = Value
  val wantsExemption = Value
  val racehorsesOrLandAndProperty = Value
  val success = Value

  implicit def Version1EligibilityResultValueToString(value: Version1EligibilityResult.Value): String = value.toString

  implicit val format = Format(Reads.enumNameReads(Version1EligibilityResult), Writes.enumNameWrites)
}

