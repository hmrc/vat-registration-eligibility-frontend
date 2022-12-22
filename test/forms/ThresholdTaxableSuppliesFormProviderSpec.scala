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

package forms

import base.SpecBase
import forms.behaviours.BooleanFieldBehaviours
import models.DateFormElement
import play.api.data.FormError
import utils.TimeMachine

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ThresholdTaxableSuppliesFormProviderSpec extends SpecBase with BooleanFieldBehaviours {

  val testMaxDate: LocalDate = LocalDate.parse("2020-01-01")
  val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  val minDate = LocalDate.of(1973, 4, 1).format(dateFormat)
  val maxDate = testMaxDate.plusMonths(3).format(dateFormat)

  object TestTimeMachine extends TimeMachine {
    override def today: LocalDate = testMaxDate
  }

  val form = new ThresholdTaxableSuppliesFormProvider(TestTimeMachine)()(messages)

  val thresholdTaxableSuppliesDate = s"thresholdTaxableSuppliesDate"
  val errorKeyRoot = "thresholdTaxableSupplies.error"

  val dateRequiredKey = "thresholdTaxableSupplies.error.date.required"
  val dateTooFarInFutureKey = "thresholdTaxableSupplies.error.date.range.max"
  val dateTooFarInPastKey = "thresholdTaxableSupplies.error.date.range.min"
  val dateInvalidKey = "thresholdTaxableSupplies.error.date.invalid"

  val dateFieldName = "thresholdTaxableSuppliesDate"
  val dayFieldName = s"$dateFieldName.day"
  val monthFieldName = s"$dateFieldName.month"
  val yearFieldName = s"$dateFieldName.year"
  val requiredKey = "thresholdTaxableSupplies.error.date.required"


  "bind" must {
    "return errors" when {
      "nothing is selected" in {
        form.bind(Map("" -> "")).errors mustBe Seq(FormError(dateFieldName, requiredKey, Seq()))
      }

      "an invalid date is provided" in {
        val date = testMaxDate.plusMonths(3)
        form.bind(
          Map(
            s"$dateFieldName.day" -> s"${date.getDayOfMonth}",
            s"$dateFieldName.month" -> s"sdsdf",
            s"$dateFieldName.year" -> s"${date.getYear}"
          )
        ).errors mustBe Seq(FormError(monthFieldName, dateInvalidKey))
      }

      "a date in the future is provided" in {
        val date = testMaxDate.plusYears(10)
        form.bind(
          Map(
            s"$dateFieldName.day" -> s"${date.getDayOfMonth}",
            s"$dateFieldName.month" -> s"${date.getMonthValue}",
            s"$dateFieldName.year" -> s"${date.getYear}"
          )
        ).errors mustBe Seq(FormError(dateFieldName, dateTooFarInFutureKey, Seq(maxDate)))
      }

      "a date in the past is provided" in {
        val date = LocalDate.of(1972, 1, 1)
        form.bind(
          Map(
            s"$dateFieldName.day" -> s"${date.getDayOfMonth}",
            s"$dateFieldName.month" -> s"${date.getMonthValue}",
            s"$dateFieldName.year" -> s"${date.getYear}"
          )
        ).errors mustBe Seq(FormError(dateFieldName, dateTooFarInPastKey, Seq(minDate)))
      }
    }

    "return a DateFormElement" when {
      "a full date is passed in" in {
        val date = testMaxDate
        form.bind(
          Map(
            s"$dateFieldName.day" -> s"${date.getDayOfMonth}",
            s"$dateFieldName.month" -> s"${date.getMonthValue}",
            s"$dateFieldName.year" -> s"${date.getYear}"
          )
        ).value mustBe Some(DateFormElement(date))
      }
    }
  }
}
