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
import models.DateFormElement
import play.api.data.FormError
import utils.TimeMachine

import java.time.LocalDate

class  DateOfBusinessTransferFormProviderSpec extends SpecBase with BooleanFieldBehaviours {

  val testDate: LocalDate = LocalDate.now()
  val testMaxDate: LocalDate = testDate.plusMonths(3)
  val testMinDate: LocalDate = LocalDate.parse("1973-04-01")

  object TestTimeMachine extends TimeMachine {
    override def today: LocalDate = testDate
  }

  val togc = "togc"
  val cole = "cole"
  
  val relevantDate = "dateOfBusinessTransfer"
  val errorKeyRoot = "dateOfBusinessTransfer.error"

  def dateRequiredKey(togcColeKey: String) = s"dateOfBusinessTransfer.error.$togcColeKey.date.required"
  val minDateKey = s"dateOfBusinessTransfer.error.date.minDate"
  val maxDateKey = s"dateOfBusinessTransfer.error.date.maxDate"
  val dateInvalidKey = s"dateOfBusinessTransfer.error.date.invalid"

  val dateFieldName = "relevantDate"

  s"bind in $togc form" must {
    val form = new DateOfBusinessTransferFormProvider(TestTimeMachine)(togc)(messages)
    "return errors" when {
      "nothing is selected" in {
        form.bind(Map("" -> "")).errors mustBe Seq(FormError(dateFieldName, dateRequiredKey(togc), Seq()))
      }

      "an invalid date is provided" in {
        form.bind(
          Map(
            s"$dateFieldName.day" -> s"${testDate.getDayOfMonth}",
            s"$dateFieldName.month" -> s"sdsdf",
            s"$dateFieldName.year" -> s"${testDate.getYear}"
          )
        ).errors mustBe Seq(FormError(dateFieldName, dateInvalidKey))
      }

      "a date in the future is provided" in {
        val date = testMaxDate.plusDays(1)
        form.bind(
          Map(
            s"$dateFieldName.day" -> s"${date.getDayOfMonth}",
            s"$dateFieldName.month" -> s"${date.getMonthValue}",
            s"$dateFieldName.year" -> s"${date.getYear}"
          )
        ).errors.headOption.map(_.message) mustBe Some(maxDateKey)
      }

      "a date in the past is provided" in {
        val date = testMinDate.minusDays(1)
        form.bind(
          Map(
            s"$dateFieldName.day" -> s"${date.getDayOfMonth}",
            s"$dateFieldName.month" -> s"${date.getMonthValue}",
            s"$dateFieldName.year" -> s"${date.getYear}"
          )
        ).errors.headOption.map(_.message) mustBe Some(minDateKey)
      }
    }

    "return a DateFormElement" when {
      "a date is within the allowed date range" in {
        val date = testMaxDate.minusMonths(6)
        form.bind(
          Map(
            s"$dateFieldName.day" -> s"${date.getDayOfMonth}",
            s"$dateFieldName.month" -> s"${date.getMonthValue}",
            s"$dateFieldName.year" -> s"${date.getYear}"
          )
        ).value mustBe Some(DateFormElement(date))
      }
      "a date is lower bound date in the allowed date range" in {
        form.bind(
          Map(
            s"$dateFieldName.day" -> s"${testMinDate.getDayOfMonth}",
            s"$dateFieldName.month" -> s"${testMinDate.getMonthValue}",
            s"$dateFieldName.year" -> s"${testMinDate.getYear}"
          )
        ).value mustBe Some(DateFormElement(testMinDate))
      }
      "a date is upper bound date in the allowed date range" in {
        form.bind(
          Map(
            s"$dateFieldName.day" -> s"${testMaxDate.getDayOfMonth}",
            s"$dateFieldName.month" -> s"${testMaxDate.getMonthValue}",
            s"$dateFieldName.year" -> s"${testMaxDate.getYear}"
          )
        ).value mustBe Some(DateFormElement(testMaxDate))
      }
    }
  }

  s"bind in $cole form" must {
    val form = new DateOfBusinessTransferFormProvider(TestTimeMachine)(cole)(messages)
    "return errors" when {
      "nothing is selected" in {
        form.bind(Map("" -> "")).errors mustBe Seq(FormError(dateFieldName, dateRequiredKey(cole), Seq()))
      }

      "an invalid date is provided" in {
        form.bind(
          Map(
            s"$dateFieldName.day" -> s"${testDate.getDayOfMonth}",
            s"$dateFieldName.month" -> s"sdsdf",
            s"$dateFieldName.year" -> s"${testDate.getYear}"
          )
        ).errors mustBe Seq(FormError(dateFieldName, dateInvalidKey))
      }

      "a date in the future is provided" in {
        val date = testMaxDate.plusDays(1)
        form.bind(
          Map(
            s"$dateFieldName.day" -> s"${date.getDayOfMonth}",
            s"$dateFieldName.month" -> s"${date.getMonthValue}",
            s"$dateFieldName.year" -> s"${date.getYear}"
          )
        ).errors.headOption.map(_.message) mustBe Some(maxDateKey)
      }

      "a date in the past is provided" in {
        val date = testMinDate.minusDays(1)
        form.bind(
          Map(
            s"$dateFieldName.day" -> s"${date.getDayOfMonth}",
            s"$dateFieldName.month" -> s"${date.getMonthValue}",
            s"$dateFieldName.year" -> s"${date.getYear}"
          )
        ).errors.headOption.map(_.message) mustBe Some(minDateKey)
      }
    }

    "return a DateFormElement" when {
      "a date is within the allowed date range" in {
        val date = testMaxDate.minusMonths(6)
        form.bind(
          Map(
            s"$dateFieldName.day" -> s"${date.getDayOfMonth}",
            s"$dateFieldName.month" -> s"${date.getMonthValue}",
            s"$dateFieldName.year" -> s"${date.getYear}"
          )
        ).value mustBe Some(DateFormElement(date))
      }
      "a date is lower bound date in the allowed date range" in {
        form.bind(
          Map(
            s"$dateFieldName.day" -> s"${testMinDate.getDayOfMonth}",
            s"$dateFieldName.month" -> s"${testMinDate.getMonthValue}",
            s"$dateFieldName.year" -> s"${testMinDate.getYear}"
          )
        ).value mustBe Some(DateFormElement(testMinDate))
      }
      "a date is upper bound date in the allowed date range" in {
        form.bind(
          Map(
            s"$dateFieldName.day" -> s"${testMaxDate.getDayOfMonth}",
            s"$dateFieldName.month" -> s"${testMaxDate.getMonthValue}",
            s"$dateFieldName.year" -> s"${testMaxDate.getYear}"
          )
        ).value mustBe Some(DateFormElement(testMaxDate))
      }
    }
  }
}
