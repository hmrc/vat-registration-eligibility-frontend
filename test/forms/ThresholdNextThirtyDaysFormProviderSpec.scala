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

package forms

import base.SpecBase
import forms.behaviours.BooleanFieldBehaviours
import models.ConditionalDateFormElement
import play.api.data.FormError
import services.ThresholdService
import utils.TimeMachine

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ThresholdNextThirtyDaysFormProviderSpec extends BooleanFieldBehaviours with SpecBase with ThresholdService {

  val testMaxDate: LocalDate = LocalDate.parse("2020-01-01")
  val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

  object TestTimeMachine extends TimeMachine {
    override def today: LocalDate = testMaxDate
  }
  implicit val msgs = messages
  val form = new ThresholdNextThirtyDaysFormProvider(TestTimeMachine)(formattedVatThreshold)

  val selectionFieldName = s"value"
  val dateFieldName = s"thresholdNextThirtyDaysDate"
  val requiredKey = "thresholdNextThirtyDays.error.required"
  val dateRequiredKey = "thresholdNextThirtyDays.error.date.required"
  val dateInFutureKey = "thresholdNextThirtyDays.error.date.inFuture"
  val dateInvalidKey = "thresholdNextThirtyDays.error.date.invalid"

  "bind" must {
    "return errors" when {
      "nothing is selected" in {
        form.bind(Map("" -> "")).errors mustBe Seq(FormError(selectionFieldName, messages(requiredKey, formattedVatThreshold()), Seq()))
      }

      "yes is selected but no date is provided" in {
        form.bind(Map(selectionFieldName -> "true")).errors mustBe Seq(FormError(dateFieldName, messages(dateRequiredKey, formattedVatThreshold()), Seq()))
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
        ).errors mustBe Seq(FormError(dateFieldName, dateInvalidKey))
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
        ).errors mustBe Seq(FormError(dateFieldName, dateInFutureKey))
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
        ).value mustBe Some(ConditionalDateFormElement(value = true, Some(date)))
      }

      "no is selected" in {
        form.bind(Map(selectionFieldName -> "false")).value mustBe Some(ConditionalDateFormElement(value = false, None))
      }
    }
  }
}
