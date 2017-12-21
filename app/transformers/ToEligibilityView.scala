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

package transformers

import common.enums.{Version1EligibilityResult => ResultV1}
import models.view.Eligibility

object ToEligibilityView {
  private val T = Some(true)
  private val F = Some(false)
  private val N = None

  def fromAPIVersion1(result: String): Eligibility = {
    ResultV1.withName(result) match {
      case ResultV1.noNino                      => Eligibility(F,N,N,N,N,N)
      case ResultV1.doingInternationalBusiness  => Eligibility(T,T,N,N,N,N)
      case ResultV1.otherInvolvementsOrCOLE     => Eligibility(T,F,T,N,N,N)
      case ResultV1.wantsAFRSOrAAS              => Eligibility(T,F,F,T,N,N)
      case ResultV1.wantsExemption              => Eligibility(T,F,F,F,T,N)
      case ResultV1.racehorsesOrLandAndProperty => Eligibility(T,F,F,F,F,T)
      case ResultV1.success                     => Eligibility(T,F,F,F,F,F)
    }
  }
}
