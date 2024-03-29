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

import forms.mappings.Mappings
import identifiers.ThresholdPreviousThirtyDaysId
import models.ConditionalDateFormElement
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}
import utils.TimeMachine

import java.time.LocalDate
import javax.inject.{Inject, Singleton}

@Singleton
class ThresholdPreviousThirtyDaysFormProvider @Inject()(timeMachine: TimeMachine) extends Mappings {

  val thresholdPreviousThirtyDaysSelection = "value"
  val thresholdPreviousThirtyDaysDate = s"${ThresholdPreviousThirtyDaysId}Date"
  val errorKeyRoot = s"$ThresholdPreviousThirtyDaysId.error"
  val valueRequiredKey = s"$errorKeyRoot.required"
  val dateRequiredKey = s"$errorKeyRoot.date.required"
  val dateInFutureKey = s"$errorKeyRoot.date.inFuture"
  val dateInvalidKey = s"$errorKeyRoot.date.invalid"
  def now = LocalDate.now()

  def apply(vatThreshold: String)(implicit messages: Messages): Form[ConditionalDateFormElement] = Form(
    mapping(
      thresholdPreviousThirtyDaysSelection -> boolean(messages(valueRequiredKey, vatThreshold)),
      thresholdPreviousThirtyDaysDate -> mandatoryIf(isEqual(thresholdPreviousThirtyDaysSelection, "true"),
        tuple(
          "day" -> default(text(), ""),
          "month" -> default(text(), ""),
          "year" -> default(text(), "")
        ).verifying(firstError(
          nonEmptyDate(messages(dateRequiredKey, vatThreshold)),
          validDate(dateInvalidKey))
        ).transform[LocalDate](
          {case (day, month, year) => LocalDate.of(year.toInt, month.toInt, day.toInt)},
          date => (date.getDayOfMonth.toString, date.getMonthValue.toString, date.getYear.toString)
        ).verifying(maxDate(timeMachine.today, dateInFutureKey))
      ))(ConditionalDateFormElement.apply)(ConditionalDateFormElement.unapply)
    )
}
