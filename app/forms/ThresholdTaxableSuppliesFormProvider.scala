/*
 * Copyright 2021 HM Revenue & Customs
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
import identifiers.ThresholdTaxableSuppliesId
import models.DateFormElement
import play.api.data.Form
import play.api.data.Forms._
import utils.TimeMachine

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

@Singleton
class ThresholdTaxableSuppliesFormProvider @Inject()(timeMachine: TimeMachine) extends FormErrorHelper with Mappings {

  val thresholdTaxableSuppliesDate = s"${ThresholdTaxableSuppliesId}Date"
  val errorKeyRoot = "thresholdTaxableSupplies.error"

  val dateRequiredKey = s"$errorKeyRoot.date.required"
  val dateOutsideRangeKey = s"$errorKeyRoot.date.inFuture"
  val dateInvalidKey = s"$errorKeyRoot.date.invalid"

  val minDateAllowed = timeMachine.today.minusYears(4)
  val maxDateAllowed = timeMachine.today.plusMonths(3)

  val dateFormat: DateTimeFormatter = DateTimeFormatter
    .ofLocalizedDate(java.time.format.FormatStyle.LONG)
    .withLocale(java.util.Locale.UK)

  def now: LocalDate = LocalDate.now()

  def apply(): Form[DateFormElement] = Form(
    mapping(
      thresholdTaxableSuppliesDate ->
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
          withinDateRange(minDateAllowed, maxDateAllowed, dateOutsideRangeKey, List(minDateAllowed.format(dateFormat), maxDateAllowed.format(dateFormat)))
        )
    )(DateFormElement.apply)(DateFormElement.unapply)
  )
}
