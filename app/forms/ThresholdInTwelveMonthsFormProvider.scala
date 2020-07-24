/*
 * Copyright 2020 HM Revenue & Customs
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
import identifiers.ThresholdInTwelveMonthsId
import javax.inject.Inject
import models.ConditionalDateFormElement
import play.api.data.Form
import play.api.data.Forms._
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

class ThresholdInTwelveMonthsFormProvider @Inject() extends FormErrorHelper with Mappings {

  val thresholdInTwelveMonthsSelection = s"${ThresholdInTwelveMonthsId}Selection"
  val thresholdInTwelveMonthsDate = s"${ThresholdInTwelveMonthsId}Date"
  val errorKeyRoot = s"$ThresholdInTwelveMonthsId.error"
  val valueRequiredKey = s"$errorKeyRoot.required"
  val dateRequiredKey = s"$errorKeyRoot.date.required"
  val dateInFutureKey = s"$errorKeyRoot.date.inFuture"
  val dateBeforeIncorpKey = s"$errorKeyRoot.date.beforeIncorp"
  val dateInvalidKey = s"$errorKeyRoot.date.invalid"
  val dateFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy")

  def now = LocalDate.now()

  def apply(incorpDate: LocalDate): Form[ConditionalDateFormElement]  = Form(
    mapping(
      thresholdInTwelveMonthsSelection -> boolean(valueRequiredKey),
      thresholdInTwelveMonthsDate -> mandatoryIf(isEqual(thresholdInTwelveMonthsSelection, "true"),
        tuple(
          "month" -> default(text(), ""),
          "year" -> default(text(), "")
        ).verifying(firstError(
            nonEmptyPartialDate(dateRequiredKey),
            validPartialDate(dateInvalidKey))
        ).transform[LocalDate](
          {case (month, year) => LocalDate.of(year.toInt, month.toInt, 1)},
          date => (date.getMonthValue.toString, date.getYear.toString)
        ).verifying(withinDateRange(incorpDate.withDayOfMonth(1), now, dateBeforeIncorpKey, dateInFutureKey, minArgs = Seq(dateFormat.format(incorpDate))))
    ))(ConditionalDateFormElement.apply)(ConditionalDateFormElement.unapply)
  )
}
