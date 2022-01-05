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

import forms.behaviours.BooleanFieldBehaviours
import models.DateFormElement
import play.api.data.FormError
import utils.TimeMachine

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ThresholdTaxableSuppliesFormProviderSpec extends BooleanFieldBehaviours {

  val testMaxDate: LocalDate = LocalDate.parse("2020-01-01")
  val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

  val minDate = testMaxDate.minusYears(4).format(dateFormat)
  val maxDate = testMaxDate.plusMonths(3).format(dateFormat)

  object TestTimeMachine extends TimeMachine {
    override def today: LocalDate = testMaxDate
  }

  val form = new ThresholdTaxableSuppliesFormProvider(TestTimeMachine)()

  val thresholdTaxableSuppliesDate = s"thresholdTaxableSuppliesDate"
  val errorKeyRoot = "thresholdTaxableSupplies.error"

  val dateRequiredKey = "thresholdTaxableSupplies.error.date.required"
  val dateOutsideRangeKey = "thresholdTaxableSupplies.error.date.inFuture"
  val dateInvalidKey = "thresholdTaxableSupplies.error.date.invalid"

  val selectionFieldName = s"thresholdTaxableSuppliesDate"
  val dateFieldName = s"thresholdTaxableSuppliesDate"
  val requiredKey = "thresholdTaxableSupplies.error.date.required"


  "bind" should {
    "return errors" when {
      "nothing is selected" in {
        form.bind(Map("" -> "")).errors shouldBe Seq(FormError(selectionFieldName, requiredKey, Seq()))
      }

      "an invalid date is provided" in {
        val date = testMaxDate.plusMonths(3)
        form.bind(
          Map(
            s"$dateFieldName.day" -> s"${date.getDayOfMonth}",
            s"$dateFieldName.month" -> s"sdsdf",
            s"$dateFieldName.year" -> s"${date.getYear}"
          )
        ).errors shouldBe Seq(FormError(dateFieldName, dateInvalidKey))
      }

      "a date in the future is provided" in {
        val date = testMaxDate.plusYears(10)
        form.bind(
          Map(
            s"$dateFieldName.day" -> s"${date.getDayOfMonth}",
            s"$dateFieldName.month" -> s"${date.getMonthValue}",
            s"$dateFieldName.year" -> s"${date.getYear}"
          )
        ).errors shouldBe Seq(FormError(dateFieldName, dateOutsideRangeKey, Seq(minDate, maxDate)))
      }

      "a date in the past is provided" in {
        val date = testMaxDate.minusYears(10)
        form.bind(
          Map(
            s"$dateFieldName.day" -> s"${date.getDayOfMonth}",
            s"$dateFieldName.month" -> s"${date.getMonthValue}",
            s"$dateFieldName.year" -> s"${date.getYear}"
          )
        ).errors shouldBe Seq(FormError(dateFieldName, dateOutsideRangeKey, Seq(minDate, maxDate)))
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
        ).value shouldBe Some(DateFormElement(date))
      }
    }
  }
}
