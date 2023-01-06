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

import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.i18n.Messages
import utils.MessageDateFormat

import java.time.LocalDate
import java.time.temporal.{ChronoField, ValueRange}
import scala.util.{Failure, Success, Try}

/*
  This formatter is defined using 'errorKeyRoot" for the error messages.
  By default it will ensure the fields are not empty and that they correspond to a real date
  It also accepts optional Min/Max dates to enable range validation
  It requires an implicit Messages to handle the translation of Min/Max dates to format them as arguments

  This formatter may return more than one FormError for cases where two fields are missing/invalid
    in that case all FormErrors will have the same message and the first one will correspond to the first
    field with an issue, this allows us to put only the head of the list into the errorHandler while still
    allowing us to highlight all invalid fields in the form itself.

  For it to work properly these error messages need to be defined:
    s"$errorKeyRoot.date.required"
    s"$errorKeyRoot.date.required.day"
    s"$errorKeyRoot.date.required.dayMonth"
    s"$errorKeyRoot.date.required.dayYear"
    s"$errorKeyRoot.date.required.month"
    s"$errorKeyRoot.date.required.monthYear"
    s"$errorKeyRoot.date.required.year"
    s"$errorKeyRoot.date.invalid"

  Optionally for the Min/Max dates (will also attempt to pass formatted dates into the error message as arguments):
    s"$errorKeyRoot.date.range.min"
    s"$errorKeyRoot.date.range.max"
 */

//scalastyle:off
case class LocalDateFormatter(errorKeyRoot: String,
                              optMinDate: Option[LocalDate] = None,
                              optMaxDate: Option[LocalDate] = None)
                             (implicit messages: Messages) extends Formatter[LocalDate] {

  val dateRequiredKey = s"$errorKeyRoot.date.required"
  val dayRequiredKey = s"$errorKeyRoot.date.required.day"
  val dayMonthRequiredKey = s"$errorKeyRoot.date.required.dayMonth"
  val dayYearRequiredKey = s"$errorKeyRoot.date.required.dayYear"
  val monthRequiredKey = s"$errorKeyRoot.date.required.month"
  val monthYearRequiredKey = s"$errorKeyRoot.date.required.monthYear"
  val yearRequiredKey = s"$errorKeyRoot.date.required.year"

  val dateInvalidKey = s"$errorKeyRoot.date.invalid"

  val dateMinError = s"$errorKeyRoot.date.range.min"
  val dateMaxError = s"$errorKeyRoot.date.range.max"

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {
    val dayKey = s"$key.day"
    val monthKey = s"$key.month"
    val yearKey = s"$key.year"
    val optDay = data.get(dayKey).filter(_.nonEmpty)
    val optMonth = data.get(monthKey).filter(_.nonEmpty)
    val optYear = data.get(yearKey).filter(_.nonEmpty)

    def dateFieldsPresent(optDay: Option[String], optMonth: Option[String], optYear: Option[String]): Either[Seq[FormError], (String, String, String)] = {
      (optDay, optMonth, optYear) match {
        case (Some(day), Some(month), Some(year)) =>
          Right(day, month, year)
        case (None, Some(_), Some(_)) =>
          Left(Seq(FormError(dayKey, dayRequiredKey)))
        case (None, None, Some(_)) =>
          Left(Seq(FormError(dayKey, dayMonthRequiredKey), FormError(monthKey, dayMonthRequiredKey)))
        case (None, Some(_), None) =>
          Left(Seq(FormError(dayKey, dayYearRequiredKey), FormError(yearKey, dayYearRequiredKey)))
        case (Some(_), None, Some(_)) =>
          Left(Seq(FormError(monthKey, monthRequiredKey)))
        case (Some(_), None, None) =>
          Left(Seq(FormError(monthKey, monthYearRequiredKey), FormError(yearKey, monthYearRequiredKey)))
        case (Some(_), Some(_), None) =>
          Left(Seq(FormError(yearKey, yearRequiredKey)))
        case (None, None, None) =>
          Left(Seq(FormError(key, dateRequiredKey)))
      }
    }

    def toDate(day: String, month: String, year: String): Either[Seq[FormError], LocalDate] = {
      def validateDay: Seq[FormError] =
        Try(ChronoField.DAY_OF_MONTH.checkValidIntValue(day.toInt)) match {
          case Success(_) => Nil
          case Failure(_) => Seq(FormError(dayKey, dateInvalidKey))
        }

      def validateMonth: Seq[FormError] =
        Try(ChronoField.MONTH_OF_YEAR.checkValidIntValue(month.toInt)) match {
          case Success(_) => Nil
          case Failure(_) => Seq(FormError(monthKey, dateInvalidKey))
        }

      def validateYear: Seq[FormError] = {
        Try(ValueRange.of(999, 9999).checkValidIntValue(year.toInt, ChronoField.YEAR)) match {
          case Success(_) => Nil
          case Failure(_) => Seq(FormError(yearKey, dateInvalidKey))
        }
      }

      validateDay ++ validateMonth ++ validateYear match {
        case Nil =>
          Try(LocalDate.of(year.toInt, month.toInt, day.toInt)) match {
            case Success(date) =>
              Right(date)
            case Failure(_) =>
              Left(Seq(FormError(key, dateInvalidKey)))
          }
        case list if list.size.equals(3) =>
          Left(Seq(FormError(key, dateInvalidKey)))
        case list =>
          Left(list)
      }
    }

    def dateWithinRange(date: LocalDate): Either[Seq[FormError], LocalDate] = {
      if (optMinDate.exists(date.isBefore)) {
        Left(Seq(FormError(key, dateMinError, optMinDate.toSeq.map(MessageDateFormat.format))))
      } else if (optMaxDate.exists(date.isAfter)) {
        Left(Seq(FormError(key, dateMaxError, optMaxDate.toSeq.map(MessageDateFormat.format))))
      } else {
        Right(date)
      }
    }

    for {
      dateFields <- dateFieldsPresent(optDay, optMonth, optYear)
      validatedDate <- toDate(dateFields._1, dateFields._2, dateFields._3)
      date <- dateWithinRange(validatedDate)
    } yield date
  }

  override def unbind(key: String, value: LocalDate): Map[String, String] = {
    Map(
      s"$key.day" -> value.getDayOfMonth.toString,
      s"$key.month" -> value.getMonthValue.toString,
      s"$key.year" -> value.getYear.toString
    )
  }

}
