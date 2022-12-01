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

import forms.mappings.Mappings
import models.DateFormElement
import play.api.data.Form
import play.api.data.Forms._
import utils.TimeMachine

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

@Singleton
class DateOfBusinessTransferFormProvider @Inject()(timeMachine: TimeMachine) extends Mappings {

  val relevantDate = "relevantDate"

  val minDateAllowed = LocalDate.parse("1973-04-01")
  val maxDateAllowed = timeMachine.today.plusMonths(3)

  val dateFormat: DateTimeFormatter = DateTimeFormatter
    .ofLocalizedDate(java.time.format.FormatStyle.LONG)
    .withLocale(java.util.Locale.UK)

  def now: LocalDate = LocalDate.now()

  def apply(togcColeKey: String): Form[DateFormElement] = {

    val errorKeyRoot = s"dateOfBusinessTransfer.error"

    val dateRequiredKey = s"$errorKeyRoot.$togcColeKey.date.required"
    val minDateAllowedKey = s"$errorKeyRoot.date.minDate"
    val maxDateAllowedKey = s"$errorKeyRoot.date.maxDate"
    val dateInvalidKey = s"$errorKeyRoot.date.invalid"
    Form(
      mapping(
        relevantDate ->
          tuple(
            "day" -> default(text(), ""),
            "month" -> default(text(), ""),
            "year" -> default(text(), "")
          ).verifying(firstError(
            nonEmptyDate(dateRequiredKey),
            validDate(dateInvalidKey))
          ).transform[LocalDate](
            { case (day, month, year) => LocalDate.of(year.toInt, month.toInt, day.toInt) },
            date => (date.getDayOfMonth.toString, date.getMonthValue.toString, date.getYear.toString)
          ).verifying(
            minDate(minDateAllowed, minDateAllowedKey, minDateAllowed.format(dateFormat)),
            maxDate(maxDateAllowed, maxDateAllowedKey, maxDateAllowed.format(dateFormat))
          )
      )(DateFormElement.apply)(DateFormElement.unapply)
    )
  }
}
