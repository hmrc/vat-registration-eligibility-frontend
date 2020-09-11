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

class ThresholdInTwelveMonthsFormProviderSpec extends BooleanFieldBehaviours {

  val testMaxDate: LocalDate = LocalDate.parse("2020-01-01")

  object TestTimeMachine extends TimeMachine {
    override def today: LocalDate = testMaxDate
  }

  val requiredKey = "thresholdInTwelveMonths.error.required"
  val dateFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy")
  val optionalDateForm = new ThresholdInTwelveMonthsFormProvider(TestTimeMachine)()

  "bind" should {
    val selectionFieldName = "value"
    val dateFieldName = "valueDate"
    val dateRequiredKey = "thresholdInTwelveMonths.error.date.required"
    val dateInFutureKey = "thresholdInTwelveMonths.error.date.inFuture"
    val dateInvalidKey = "thresholdInTwelveMonths.error.date.invalid"

    "return errors" when {
      "nothing is selected" in {
        optionalDateForm.bind(Map("" -> "")).errors shouldBe Seq(FormError(selectionFieldName, requiredKey, Seq()))
      }

      "yes is selected but no date is provided" in {
        optionalDateForm.bind(Map(selectionFieldName -> "true")).errors shouldBe Seq(FormError(dateFieldName, dateRequiredKey, Seq()))
      }

      "yes is selected but an invalid date is provided" in {
        val date = LocalDate.now().plusMonths(3)
        optionalDateForm.bind(
          Map(
            selectionFieldName -> "true",
            s"${dateFieldName}.month" -> s"sdsdf",
            s"${dateFieldName}.year" -> s"${date.getYear}")
        ).errors shouldBe Seq(FormError(dateFieldName, dateInvalidKey))
      }

      "yes is selected but a date in the future is provided" in {
        val date = LocalDate.now().plusMonths(3)
        optionalDateForm.bind(
          Map(
            selectionFieldName -> "true",
            s"${dateFieldName}.month" -> s"${date.getMonthValue}",
            s"${dateFieldName}.year" -> s"${date.getYear}")
        ).errors shouldBe Seq(FormError(dateFieldName, dateInFutureKey))
      }
    }

    "return a ConditionalFromElement" when {
      "yes is selected and a month and year is passed in" in {
        val date = testMaxDate
        optionalDateForm.bind(
          Map(
            selectionFieldName -> "true",
            s"${dateFieldName}.month" -> s"${date.getMonthValue}",
            s"${dateFieldName}.year" -> s"${date.getYear}")
        ).value shouldBe Some(ConditionalDateFormElement(true, Some(date)))
      }
      "yes is selected and date entered is after Test date but is valid because both are defaulted to the 1st of the month" in {
        val dateEntered = LocalDate.of(2018, 5, 1)
        new ThresholdInTwelveMonthsFormProvider(TestTimeMachine)().bind(
          Map(
            selectionFieldName -> "true",
            s"${dateFieldName}.month" -> s"${dateEntered.getMonthValue}",
            s"${dateFieldName}.year" -> s"${dateEntered.getYear}")
        ).value shouldBe Some(ConditionalDateFormElement(true, Some(dateEntered)))
      }
      "no is selected" in {
        optionalDateForm.bind(Map(selectionFieldName -> "false")).value shouldBe Some(ConditionalDateFormElement(false, None))
      }
    }
  }
}