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

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import forms.mappings.Mappings
import identifiers.ThresholdPreviousThirtyDaysId
import javax.inject.{Inject, Singleton}
import models.ConditionalDateFormElement
import play.api.data.Form
import play.api.data.Forms._
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}
import utils.TimeMachine

@Singleton
class ThresholdPreviousThirtyDaysFormProvider @Inject()(timeMachine: TimeMachine) extends FormErrorHelper with Mappings {

  val thresholdPreviousThirtyDaysSelection = "value"
  val thresholdPreviousThirtyDaysDate = s"${ThresholdPreviousThirtyDaysId}Date"
  val errorKeyRoot = s"$ThresholdPreviousThirtyDaysId.error"
  val valueRequiredKey = s"$errorKeyRoot.required"
  val dateRequiredKey = s"$errorKeyRoot.date.required"
  val dateInFutureKey = s"$errorKeyRoot.date.inFuture"
  val dateInvalidKey = s"$errorKeyRoot.date.invalid"
  val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
  def now = LocalDate.now()

  def apply(): Form[ConditionalDateFormElement] = Form(
    mapping(
      thresholdPreviousThirtyDaysSelection -> boolean(valueRequiredKey),
      thresholdPreviousThirtyDaysDate -> mandatoryIf(isEqual(thresholdPreviousThirtyDaysSelection, "true"),
        tuple(
          "day" -> default(text(), ""),
          "month" -> default(text(), ""),
          "year" -> default(text(), "")
        ).verifying(firstError(
          nonEmptyDate(dateRequiredKey),
          validDate(dateInvalidKey))
        ).transform[LocalDate](
          {case (day, month, year) => LocalDate.of(year.toInt, month.toInt, day.toInt)},
          date => (date.getDayOfMonth.toString, date.getMonthValue.toString, date.getYear.toString)
        ).verifying(maxDate(timeMachine.today, dateInFutureKey))
      ))(ConditionalDateFormElement.apply)(ConditionalDateFormElement.unapply)
    )
}
