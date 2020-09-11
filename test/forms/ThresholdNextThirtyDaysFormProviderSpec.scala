/*
 * Copyright 2020 HM Revenue & Customs
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

package forms

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import forms.behaviours.BooleanFieldBehaviours
import models.ConditionalDateFormElement
import play.api.data.FormError
import utils.TimeMachine

class ThresholdNextThirtyDaysFormProviderSpec extends BooleanFieldBehaviours {

  val testMaxDate: LocalDate = LocalDate.parse("2020-01-01")
  val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

  object TestTimeMachine extends TimeMachine {
    override def today: LocalDate = testMaxDate
  }

  val form = new ThresholdNextThirtyDaysFormProvider(TestTimeMachine)()

  val selectionFieldName = s"value"
  val dateFieldName = s"thresholdNextThirtyDaysDate"
  val requiredKey = "thresholdNextThirtyDays.error.required"
  val dateRequiredKey = "thresholdNextThirtyDays.error.date.required"
  val dateInFutureKey = "thresholdNextThirtyDays.error.date.inFuture"
  val dateInvalidKey = "thresholdNextThirtyDays.error.date.invalid"

  "bind" should {
    "return errors" when {
      "nothing is selected" in {
        form.bind(Map("" -> "")).errors shouldBe Seq(FormError(selectionFieldName, requiredKey, Seq()))
      }

      "yes is selected but no date is provided" in {
        form.bind(Map(selectionFieldName -> "true")).errors shouldBe Seq(FormError(dateFieldName, dateRequiredKey, Seq()))
      }

      "yes is selected but an invalid date is provided" in {
        val date = testMaxDate.plusMonths(3)
        form.bind(
          Map(
            selectionFieldName -> "true",
            s"$dateFieldName.day" -> s"${date.getDayOfMonth}",
            s"$dateFieldName.month" -> s"sdsdf",
            s"$dateFieldName.year" -> s"${date.getYear}"
          )
        ).errors shouldBe Seq(FormError(dateFieldName, dateInvalidKey))
      }

      "yes is selected but a date in the future is provided" in {
        val date = testMaxDate.plusMonths(3)
        form.bind(
          Map(
            selectionFieldName -> "true",
            s"$dateFieldName.day" -> s"${date.getDayOfMonth}",
            s"$dateFieldName.month" -> s"${date.getMonthValue}",
            s"$dateFieldName.year" -> s"${date.getYear}"
          )
        ).errors shouldBe Seq(FormError(dateFieldName, dateInFutureKey))
      }
    }

    "return a ConditionalFormElement" when {
      "yes is selected and a month and year is passed in" in {
        val date = testMaxDate
        form.bind(
          Map(
            selectionFieldName -> "true",
            s"$dateFieldName.day" -> s"${date.getDayOfMonth}",
            s"$dateFieldName.month" -> s"${date.getMonthValue}",
            s"$dateFieldName.year" -> s"${date.getYear}"
          )
        ).value shouldBe Some(ConditionalDateFormElement(value = true, Some(date)))
      }

      "no is selected" in {
        form.bind(Map(selectionFieldName -> "false")).value shouldBe Some(ConditionalDateFormElement(value = false, None))
      }
    }
  }
}
