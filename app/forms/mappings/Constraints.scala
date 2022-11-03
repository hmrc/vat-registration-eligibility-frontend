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

package forms.mappings

import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import utils.RadioOption

import java.time.LocalDate
import java.time.format.{DateTimeFormatter, ResolverStyle}
import scala.util.{Success, Try}

trait Constraints {

  protected def firstError[A](constraints: Constraint[A]*): Constraint[A] =
    Constraint {
      input =>
        constraints
          .map(_.apply(input))
          .find(_ != Valid)
          .getOrElse(Valid)
    }

  private def tupleToDate(dateTuple: (String, String, String)) = {
    LocalDate.parse(s"${dateTuple._1}-${dateTuple._2}-${dateTuple._3}", DateTimeFormatter.ofPattern("d-M-uuuu").withResolverStyle(ResolverStyle.STRICT))
  }

  protected def validDate(errKey: String, args: Seq[String] = Seq()): Constraint[(String, String, String)] = Constraint {
    input: (String, String, String) =>
      val date = Try {
        tupleToDate(input)
      }.toOption
      date match {
        case Some(_) => Valid
        case None => Invalid(errKey, args: _*)
      }
  }

  protected def nonEmptyPartialDate(errKey: String, args: Seq[String] = Seq()): Constraint[(String, String)] = Constraint {
    case (_, "") | ("", _) => Invalid(errKey, args: _*)
    case _ => Valid
  }

  protected def nonEmptyDate(errKey: String, args: Seq[String] = Seq()): Constraint[(String, String, String)] = Constraint {
    case (_, _, "") | ("", _, _) | (_, "", _) => Invalid(errKey, args: _*)
    case _ => Valid
  }

  protected def validPartialDate(errKey: String, args: Seq[String] = Seq()): Constraint[(String, String)] = Constraint {
    input: (String, String) =>
      val date = Try {
        tupleToDate(("1", input._1, input._2))
      }.toOption
      date match {
        case Some(_) => Valid
        case None => Invalid(errKey, args: _*)
      }
  }

  protected def maxDate(maximum: LocalDate, errorKey: String, args: Any*): Constraint[LocalDate] =
    Constraint {
      case date if date.isAfter(maximum) =>
        Invalid(errorKey, args: _*)
      case _ =>
        Valid
    }

  protected def minDate(minimum: LocalDate, errorKey: String, args: Any*): Constraint[LocalDate] =
    Constraint {
      case date if date.isBefore(minimum) =>
        Invalid(errorKey, args: _*)
      case _ =>
        Valid
    }

  protected def withinDateRange(minDate: LocalDate, maxDate: LocalDate, error: String, args: Seq[String] = Seq()
                               ): Constraint[LocalDate] =
    Constraint { date: LocalDate =>
      if (date.isEqual(minDate) || date.isAfter(minDate)) {
        if (date.isEqual(maxDate) || date.isBefore(maxDate)) Valid else Invalid(ValidationError(error, args: _*))
      } else {
        Invalid(ValidationError(error, args: _*))
      }
    }

  protected def minimumValue[A](minimum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>
        import ev._

        if (input >= minimum) {
          Valid
        } else {
          Invalid(errorKey, Seq(minimum): _*)
        }
    }

  protected def maximumValue[A](maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>
        import ev._

        if (input <= maximum) {
          Valid
        } else {
          Invalid(errorKey, Seq(maximum): _*)
        }
    }

  protected def inRange[A](minimum: A, maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev._

        if (input >= minimum && input <= maximum) {
          Valid
        } else {
          Invalid(errorKey, minimum, maximum)
        }
    }

  protected def regexp(regex: String, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.matches(regex) =>
        Valid
      case _ =>
        Invalid(errorKey, regex)
    }

  protected def minLength(minimum: Int, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.length >= minimum =>
        Valid
      case _ =>
        Invalid(errorKey, minimum)
    }

  protected def maxLength(maximum: Int, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.length <= maximum =>
        Valid
      case _ =>
        Invalid(errorKey, maximum)
    }

  protected def validBigIntConversion(errorKey: String): Constraint[String] =
    Constraint { bigIntStr =>
      Try(BigInt(bigIntStr)) match {
        case Success(_) => Valid
        case _ => Invalid(errorKey)
      }
    }

  protected def validNino(errorKey: String): Constraint[String] =
    Constraint { str =>
      val regex = "[[A-Z]&&[^DFIQUV]][[A-Z]&&[^DFIQUVO]] ?\\d{2} ?\\d{2} ?\\d{2} ?[A-D]{1}".r

      regex.unapplySeq(str).map(_ => Valid).getOrElse(Invalid(errorKey))
    }

  protected def bigIntRange(errorKeyLess: String, errorKeyMore: String, low: BigInt, high: BigInt): Constraint[String] =
    Constraint { bigIntStr =>
      BigInt(bigIntStr) match {
        case e if e > high => Invalid(errorKeyLess, Seq(high): _*)
        case e if e <= low => Invalid(errorKeyMore, Seq(low): _*)
        case _ => Valid
      }
    }

  protected def matchesRadioSeq(radioSeq: Set[RadioOption], errKey: String): Constraint[String] = {
    Constraint {
      input: String =>
        if (radioSeq.exists {
          option => option.value == input
        }) {
          Valid
        } else {
          Invalid(errKey)
        }
    }
  }

  private def calcWeightedSum(value: String): Int = {
    // not efficient but saves writing out a hardcoded calculation or a recursive function
    val constants = (2 to 8).reverse
    value.map(_.asDigit).zip(constants)
      .map { case (digit, constant) => digit * constant }.sum
  }

  def isValidChecksum(errorKey: String): Constraint[String] = {
    Constraint { vatNumber =>
      val leading = vatNumber.substring(0, vatNumber.length - 2)
      val checksum = vatNumber.substring(vatNumber.length - 2).toInt

      val weightedSumPlusChecksum = calcWeightedSum(leading) + checksum

      if((weightedSumPlusChecksum % 97) == 0 || ((weightedSumPlusChecksum + 55) % 97) == 0) {
        Valid
      } else {
        Invalid(errorKey)
      }
    }
  }
}
