/*
 * Copyright 2023 HM Revenue & Customs
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

package forms.mappings

import base.SpecBase
import play.api.data.FormError
import utils.MessageDateFormat

import java.time.LocalDate

class LocalDateFormatterSpec extends SpecBase {

  val testErrorKey = "testErrorKey"
  val testDateRequiredKey = s"$testErrorKey.date.required"
  val testDayRequiredKey = s"$testErrorKey.date.required.day"
  val testDayMonthRequiredKey = s"$testErrorKey.date.required.dayMonth"
  val testDayYearRequiredKey = s"$testErrorKey.date.required.dayYear"
  val testMonthRequiredKey = s"$testErrorKey.date.required.month"
  val testMonthYearRequiredKey = s"$testErrorKey.date.required.monthYear"
  val testYearRequiredKey = s"$testErrorKey.date.required.year"

  val testDateInvalidKey = s"$testErrorKey.date.invalid"

  val testDateMinError = s"$testErrorKey.date.range.min"
  val testDateMaxError = s"$testErrorKey.date.range.max"

  val testDateKey = "testDateKey"
  val testDayKey = s"$testDateKey.day"
  val testMonthKey = s"$testDateKey.month"
  val testYearKey = s"$testDateKey.year"

  val testDate: LocalDate = LocalDate.of(2000, 2, 1)
  val testMinDate: LocalDate = testDate.minusYears(1)
  val testMaxDate: LocalDate = testDate.plusYears(1)

  val testFormatter: LocalDateFormatter = LocalDateFormatter(testErrorKey, Some(testMinDate), Some(testMaxDate))(messages)

  "testFormatter.bind" when {
    "there is a single empty field" must {
      "return day required error when only the day is missing" in {
        testFormatter.bind(
          testDateKey,
          Map(
            testDayKey -> "",
            testMonthKey -> "1",
            testYearKey -> "2000"
          )
        ) mustBe Left(List(FormError(testDayKey, testDayRequiredKey)))
      }
      "return month required error when only the month is missing" in {
        testFormatter.bind(
          testDateKey,
          Map(
            testDayKey -> "1",
            testMonthKey -> "",
            testYearKey -> "2000"
          )
        ) mustBe Left(List(FormError(testMonthKey, testMonthRequiredKey)))
      }
      "return year required error when only the year is missing" in {
        testFormatter.bind(
          testDateKey,
          Map(
            testDayKey -> "1",
            testMonthKey -> "1",
            testYearKey -> ""
          )
        ) mustBe Left(List(FormError(testYearKey, testYearRequiredKey)))
      }
    }
    "there are a multiple empty fields" must {
      "return day/month required errors when the day and month are missing" in {
        testFormatter.bind(
          testDateKey,
          Map(
            testDayKey -> "",
            testMonthKey -> "",
            testYearKey -> "2000"
          )
        ) mustBe Left(List(FormError(testDayKey, testDayMonthRequiredKey), FormError(testMonthKey, testDayMonthRequiredKey)))
      }
      "return day/year required errors when the day and year are missing" in {
        testFormatter.bind(
          testDateKey,
          Map(
            testDayKey -> "",
            testMonthKey -> "1",
            testYearKey -> ""
          )
        ) mustBe Left(List(FormError(testDayKey, testDayYearRequiredKey), FormError(testYearKey, testDayYearRequiredKey)))
      }
      "return month/year required errors when the month and year are missing" in {
        testFormatter.bind(
          testDateKey,
          Map(
            testDayKey -> "1",
            testMonthKey -> "",
            testYearKey -> ""
          )
        ) mustBe Left(List(FormError(testMonthKey, testMonthYearRequiredKey), FormError(testYearKey, testMonthYearRequiredKey)))
      }
      "return global required error when the day, month and year are missing" in {
        testFormatter.bind(
          testDateKey,
          Map(
            testDayKey -> "",
            testMonthKey -> "",
            testYearKey -> ""
          )
        ) mustBe Left(List(FormError(testDateKey, testDateRequiredKey)))
      }
    }
    "there is a single invalid field" must {
      "return invalid error on the day key when only the day is invalid" in {
        testFormatter.bind(
          testDateKey,
          Map(
            testDayKey -> "a",
            testMonthKey -> "1",
            testYearKey -> "2000"
          )
        ) mustBe Left(List(FormError(testDayKey, testDateInvalidKey)))
      }
      "return invalid error on the month key when only the month is invalid" in {
        testFormatter.bind(
          testDateKey,
          Map(
            testDayKey -> "1",
            testMonthKey -> "13",
            testYearKey -> "2000"
          )
        ) mustBe Left(List(FormError(testMonthKey, testDateInvalidKey)))
      }
      "return invalid error on the year key when only the year is invalid" in {
        testFormatter.bind(
          testDateKey,
          Map(
            testDayKey -> "1",
            testMonthKey -> "1",
            testYearKey -> "a"
          )
        ) mustBe Left(List(FormError(testYearKey, testDateInvalidKey)))
      }
    }
    "there are a multiple invalid fields" must {
      "return invalid errors on day/month keys when the day and month are invalid" in {
        testFormatter.bind(
          testDateKey,
          Map(
            testDayKey -> "32",
            testMonthKey -> "b",
            testYearKey -> "2000"
          )
        ) mustBe Left(List(FormError(testDayKey, testDateInvalidKey), FormError(testMonthKey, testDateInvalidKey)))
      }
      "return invalid errors on day/year keys when the day and year are invalid" in {
        testFormatter.bind(
          testDateKey,
          Map(
            testDayKey -> "-",
            testMonthKey -> "1",
            testYearKey -> "1"
          )
        ) mustBe Left(List(FormError(testDayKey, testDateInvalidKey), FormError(testYearKey, testDateInvalidKey)))
      }
      "return invalid errors on month/year keys when the month and year are invalid" in {
        testFormatter.bind(
          testDateKey,
          Map(
            testDayKey -> "1",
            testMonthKey -> "0",
            testYearKey -> "1000000"
          )
        ) mustBe Left(List(FormError(testMonthKey, testDateInvalidKey), FormError(testYearKey, testDateInvalidKey)))
      }
      "return global invalid error when the day, month and year are invalid" in {
        testFormatter.bind(
          testDateKey,
          Map(
            testDayKey -> "a",
            testMonthKey -> "b",
            testYearKey -> "c"
          )
        ) mustBe Left(List(FormError(testDateKey, testDateInvalidKey)))
      }
      "return global invalid error when the day, month and year are valid but do not produce a real date" in {
        testFormatter.bind(
          testDateKey,
          Map(
            testDayKey -> "29",
            testMonthKey -> "2",
            testYearKey -> "2022"
          )
        ) mustBe Left(List(FormError(testDateKey, testDateInvalidKey)))
      }
    }
    "the formatter has min and max dates" must {
      "return global date error when date is valid but is too far in the future" in {
        testFormatter.bind(
          testDateKey,
          Map(
            testDayKey -> "2",
            testMonthKey -> "2",
            testYearKey -> "2001"
          )
        ) mustBe Left(List(FormError(testDateKey, testDateMaxError, List(MessageDateFormat.format(testMaxDate)(messages)))))
      }
      "return global date error when date is valid but is too far in the past" in {
        testFormatter.bind(
          testDateKey,
          Map(
            testDayKey -> "30",
            testMonthKey -> "1",
            testYearKey -> "1999"
          )
        ) mustBe Left(List(FormError(testDateKey, testDateMinError, List(MessageDateFormat.format(testMinDate)(messages)))))
      }
    }
    "the date is valid" must {
      "return the date" in {
        testFormatter.bind(
          testDateKey,
          Map(
            testDayKey -> "1",
            testMonthKey -> "1",
            testYearKey -> "2000"
          )
        ) mustBe Right(LocalDate.of(2000, 1, 1))
      }
    }
    "the formatter does not have min and max dates" must {
      val formatter = LocalDateFormatter(testErrorKey)(messages)
      "return the date when date is valid in the future" in {
        formatter.bind(
          testDateKey,
          Map(
            testDayKey -> "2",
            testMonthKey -> "2",
            testYearKey -> "2001"
          )
        ) mustBe Right(LocalDate.of(2001, 2, 2))
      }
      "return the date when date is valid in the past" in {
        formatter.bind(
          testDateKey,
          Map(
            testDayKey -> "30",
            testMonthKey -> "1",
            testYearKey -> "1999"
          )
        ) mustBe Right(LocalDate.of(1999, 1, 30))
      }
    }
  }

  "testFormatter.unbind" must {
    "return a map with the correct day, month and year" in {
      testFormatter.unbind(testDateKey, testDate) mustBe Map(
        testDayKey -> testDate.getDayOfMonth.toString,
        testMonthKey -> testDate.getMonthValue.toString,
        testYearKey -> testDate.getYear.toString
      )
    }
  }
}
