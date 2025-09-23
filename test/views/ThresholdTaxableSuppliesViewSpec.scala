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

package views

import forms.ThresholdTaxableSuppliesFormProvider
import models.NormalMode
import utils.TimeMachine
import views.html.ThresholdTaxableSupplies

import java.time.LocalDate

class ThresholdTaxableSuppliesViewSpec extends ViewSpecBase {

  val messageKeyPrefix = "thresholdTaxableSupplies"

  object TestTimeMachine extends TimeMachine {
    override def today: LocalDate = LocalDate.parse("2020-01-01")
  }

  object Selectors extends BaseSelectors
  val form = new ThresholdTaxableSuppliesFormProvider(TestTimeMachine)()(messages)

  val h1 = "When did the business make or intend to make taxable supplies in the UK?"
  val testText = "You should enter the earliest date the business either made or intended to make taxable supplies in the UK."
  val testHint = "For example, 18 10 2020"
  val testButton = "Continue"

  val view = app.injector.instanceOf[ThresholdTaxableSupplies]

  "ThresholdTaxableSupplies view" must {
    val doc = asDocument(view(form, NormalMode)(fakeDataRequestIncorped, messages, frontendAppConfig))

    "have the correct continue button" in {
      doc.select(Selectors.button).text() mustBe continueButton
    }

    "have the correct back link" in {
      doc.select(Selectors.backLink).text() mustBe backLink
    }

    "have the correct browser title" in {
      doc.select(Selectors.title).first().text() mustBe title(h1)
    }

    "have the correct heading" in {
      doc.select(Selectors.h1).text() mustBe h1
    }

    "have the correct legend" in {
      doc.select(Selectors.legend(1)).text() mustBe h1
    }

    "have the correct text" in {
      doc.select(Selectors.p(1)).text() mustBe testText
    }

    "have the correct hint" in {
      doc.select(Selectors.hint).text() mustBe testHint
    }
  }
}
