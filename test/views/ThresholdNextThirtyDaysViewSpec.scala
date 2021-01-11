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

package views

import java.time.LocalDate

import forms.ThresholdNextThirtyDaysFormProvider
import models.NormalMode
import utils.TimeMachine
import views.html.thresholdNextThirtyDays

class ThresholdNextThirtyDaysViewSpec extends ViewSpecBase {

  val messageKeyPrefix = "thresholdNextThirtyDays"

  object TestTimeMachine extends TimeMachine {
    override def today: LocalDate = LocalDate.parse("2020-01-01")
  }

  object Selectors extends BaseSelectors

  val form = new ThresholdNextThirtyDaysFormProvider(TestTimeMachine)()

  val h1 = "Does the business expect to make more than £85,000 in a single month or a 30-day period?"
  val testText = "This could happen when, for example, on 16 April a business wins a big contract to supply goods or services, which would mean the value of supplies made solely within the next 30 days, by 15 May, are more than the VAT registration threshold."
  val testLegend = "What date did the business realise it would go over the threshold?"
  val testHint = "For example, 13 02 2017"
  val testButton = "Continue"

  "ThresholdNextThirtyDays view" must {
    val doc = asDocument(thresholdNextThirtyDays(form, NormalMode)(fakeDataRequestIncorped, messages, frontendAppConfig))

    "have the correct continue button" in {
      doc.select(Selectors.button).text() mustBe continueButton
    }

    "have the correct back link" in {
      doc.getElementById(Selectors.backLink).text() mustBe backLink
    }

    "have the correct browser title" in {
      doc.select(Selectors.title).text() mustBe title(h1)
    }

    "have the correct heading" in {
      doc.select(Selectors.h1).text() mustBe h1
    }

    "have the correct text" in {
      doc.select(Selectors.p(1)).text() mustBe testText
    }

    "have the correct legend" in {
      doc.select(Selectors.legend(2)).text() mustBe testLegend
    }

    "have the correct hint" in {
      doc.select(Selectors.hint).text() mustBe testHint
    }
  }
}
