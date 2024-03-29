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

package forms.mappings

import org.scalatestplus.play.PlaySpec
import play.api.data.validation.{Invalid, Valid}

import java.time.LocalDate

class ConstraintsSpec extends PlaySpec with Constraints {


  "firstError" must {

    "return Valid when all constraints pass" in {
      val result = firstError(maxLength(10, "error.length"), regexp("""^\w+$""", "error.regexp"))("foo")
      result mustEqual Valid
    }

    "return Invalid when the first constraint fails" in {
      val result = firstError(maxLength(10, "error.length"), regexp("""^\w+$""", "error.regexp"))("a" * 11)
      result mustEqual Invalid("error.length", 10)
    }

    "return Invalid when the second constraint fails" in {
      val result = firstError(maxLength(10, "error.length"), regexp("""^\w+$""", "error.regexp"))("")
      result mustEqual Invalid("error.regexp", """^\w+$""")
    }

    "return Invalid for the first error when both constraints fail" in {
      val result = firstError(maxLength(-1, "error.length"), regexp("""^\w+$""", "error.regexp"))("")
      result mustEqual Invalid("error.length", -1)
    }
  }

  "minimumValue" must {

    "return Valid for a number greater than the threshold" in {
      val result = minimumValue(1, "error.min").apply(2)
      result mustEqual Valid
    }

    "return Valid for a number equal to the threshold" in {
      val result = minimumValue(1, "error.min").apply(1)
      result mustEqual Valid
    }

    "return Invalid for a number below the threshold" in {
      val result = minimumValue(1, "error.min").apply(0)
      result mustEqual Invalid("error.min", 1)
    }
  }

  "maximumValue" must {

    "return Valid for a number less than the threshold" in {
      val result = maximumValue(1, "error.max").apply(0)
      result mustEqual Valid
    }

    "return Valid for a number equal to the threshold" in {
      val result = maximumValue(1, "error.max").apply(1)
      result mustEqual Valid
    }

    "return Invalid for a number above the threshold" in {
      val result = maximumValue(1, "error.max").apply(2)
      result mustEqual Invalid("error.max", 1)
    }
  }

  "inRange" must {

    "return Invalid for a number less than the threshold" in {
      val result = inRange(1, 3, "error.th").apply(0)
      result mustEqual Invalid("error.th", 1, 3)
    }

    "return Invalid for a number above the threshold" in {
      val result = inRange(1, 3, "error.th").apply(4)
      result mustEqual Invalid("error.th", 1, 3)
    }

    "return Valid for a number in the threshold" in {
      val result = inRange(1, 3, "error.th").apply(2)
      result mustEqual Valid
    }
  }

  "regexp" must {

    "return Valid for an input that matches the expression" in {
      val result = regexp("""^\w+$""", "error.invalid")("foo")
      result mustEqual Valid
    }

    "return Invalid for an input that does not match the expression" in {
      val result = regexp("""^\d+$""", "error.invalid")("foo")
      result mustEqual Invalid("error.invalid", """^\d+$""")
    }
  }

  "maxLength" must {

    "return Valid for a string shorter than the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 9)
      result mustEqual Valid
    }

    "return Valid for an empty string" in {
      val result = maxLength(10, "error.length")("")
      result mustEqual Valid
    }

    "return Valid for a string equal to the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 10)
      result mustEqual Valid
    }

    "return Invalid for a string longer than the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 11)
      result mustEqual Invalid("error.length", 10)
    }
  }

  "minLength" must {
    "return Valid for a string shorter than the allowed length" in {
      val result = minLength(10, "error.length")("a" * 9)
      result mustEqual Invalid("error.length", 10)
    }

    "return Valid for a string equal to the allowed length" in {
      val result = minLength(10, "error.length")("a" * 10)
      result mustEqual Valid
    }

    "return Invalid for a string shorter than the allowed length" in {
      val result = minLength(10, "error.length")("a" * 11)
      result mustEqual Valid
    }
  }

  "maxDate" must {

    val maximumDate = LocalDate.parse("2019-01-01")

    "return Valid for a date before or equal to the maximum" in {
      val result = maxDate(maximumDate, "error.date")(LocalDate.parse("2019-01-01"))
      result mustEqual Valid
    }

    "return Invalid for a date after the maximum" in {
      val result = maxDate(maximumDate, "thresholdPreviousThirtyDays.error.date.inFuture")(maximumDate.plusDays(1))
      result mustEqual Invalid("thresholdPreviousThirtyDays.error.date.inFuture")
    }
  }

  "minDate" must {

    val minimumDate = LocalDate.parse("2019-01-01")

    "return Valid for a date after or equal to the minimum" in {
      val result = minDate(minimumDate, "error.date")(LocalDate.parse("2019-01-01"))
      result mustEqual Valid
    }

    "return Invalid for a date before the minimum" in {
      val result = minDate(minimumDate, "thresholdPreviousThirtyDays.error.date.inPast")(minimumDate.minusDays(1))
      result mustEqual Invalid("thresholdPreviousThirtyDays.error.date.inPast")
    }
  }

  "isValidChecksum" must {
    "returns Valid if the checksum is correct for a mod 97 number" in {
      val testValue = "011000084"
      isValidChecksum("vatNumber.error.invalid")(testValue) mustEqual Valid
    }

    "returns Invalid if the checksum is correct for a mod 9755 number" in {
      val testValue = "011000029"
      isValidChecksum("vatNumber.error.invalid")(testValue) mustEqual Valid
    }

    "returns Invalid if the checksum is incorrect" in {
      val testValue = "999999999"
      isValidChecksum("vatNumber.error.invalid")(testValue) mustEqual Invalid("vatNumber.error.invalid")
    }
  }
}
