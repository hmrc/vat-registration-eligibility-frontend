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

package utils

import models.ConditionalDateFormElement

object ThresholdHelper {
  val q1DefinedAndTrue = (userAns: UserAnswers) => userAns.thresholdInTwelveMonths.collect { case ConditionalDateFormElement(true, _) => true }.isDefined
  val nextThirtyDaysDefinedAndTrue = (userAns: UserAnswers) => userAns.thresholdNextThirtyDays.collect { case ConditionalDateFormElement(true, _) => true }.isDefined
  val nextThirtyDaysDefinedAndFalse = (userAns: UserAnswers) => userAns.thresholdNextThirtyDays.collect { case ConditionalDateFormElement(false, _) => false }.isDefined

  val taxableTurnoverCheck = (userAns: UserAnswers) => {
    (userAns.thresholdNextThirtyDays, userAns.thresholdPreviousThirtyDays) match {
      case (Some(ConditionalDateFormElement(true, _)), Some(_)) | (Some(_), Some(ConditionalDateFormElement(true, _))) => true
      case _ => false
    }
  }
}