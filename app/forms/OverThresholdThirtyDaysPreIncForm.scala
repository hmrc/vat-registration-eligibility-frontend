/*
 * Copyright 2018 HM Revenue & Customs
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

import forms.helpers.RequiredBoolean
import play.api.data.Form
import play.api.data.Forms.single

object OverThresholdThirtyDaysPreIncForm extends RequiredBoolean {

  val RADIO_YES_NO: String = "overThresholdThirtyPreIncorpRadio"

  val errorMessage = "validation.thresholdQuestion3.missing"

  def form(taxableTurnover: String): Form[Boolean] = Form(
    single(
      RADIO_YES_NO -> requiredBoolean(errorMessage, taxableTurnover)
    )
  )
}