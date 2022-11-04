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
import identifiers.ThresholdNextThirtyDaysId
import models.ConditionalDateFormElement
import play.api.data.Form
import play.api.data.Forms._
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}
import utils.TimeMachine

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

@Singleton
class ThresholdNextThirtyDaysFormProvider @Inject()(timeMachine: TimeMachine) extends FormErrorHelper with Mappings {

  val thresholdNextThirtyDaysSelection = "value"
  val thresholdNextThirtyDaysDate = s"${ThresholdNextThirtyDaysId}Date"
  val valueRequiredKey = s"$ThresholdNextThirtyDaysId.error.required"
  val dateRequiredKey = s"$ThresholdNextThirtyDaysId.error.date.required"
  val dateInFutureKey = s"$ThresholdNextThirtyDaysId.error.date.inFuture"
  val dateInvalidKey = s"$ThresholdNextThirtyDaysId.error.date.invalid"
  val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

  def now: LocalDate = LocalDate.now()

  def apply(): Form[ConditionalDateFormElement] = Form(
    mapping(
      thresholdNextThirtyDaysSelection -> boolean(valueRequiredKey),
      thresholdNextThirtyDaysDate -> mandatoryIf(isEqual(thresholdNextThirtyDaysSelection, "true"),
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
        ).verifying(maxDate(timeMachine.today, dateInFutureKey))
      ))(ConditionalDateFormElement.apply)(ConditionalDateFormElement.unapply)
  )
}
