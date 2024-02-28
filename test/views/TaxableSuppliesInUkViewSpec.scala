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

import forms.TaxableSuppliesInUkFormProvider
import models.NormalMode
import views.html.TaxableSuppliesInUk

class TaxableSuppliesInUkViewSpec extends ViewSpecBase {

  val form = new TaxableSuppliesInUkFormProvider()()

  val h1 = "Has the business made taxable supplies in the UK?"
  val para = "Tell us if the business:"
  val bullet1 = "has made taxable supplies in the UK"
  val bullet2 = "previously intended to make taxable supplies in the UK"
  val bullet3 = "intends to make taxable supplies in the UK within the next 3 months"

  val view: TaxableSuppliesInUk = app.injector.instanceOf[TaxableSuppliesInUk]

  object Selectors extends BaseSelectors

  "Taxable Supplies In UK view" must {
    lazy val doc = asDocument(view(form, NormalMode)(fakeDataRequest, messages, frontendAppConfig))

    "have the correct continue button" in {
      doc.select(Selectors.button).text() mustBe continueButton
    }

    "have the correct back link" in {
      doc.select(Selectors.backLink).text() mustBe backLink
    }

    "have the correct browser title" in {
      doc.select(Selectors.title).text() mustBe title(h1)
    }

    "have the correct heading" in {
      doc.select(Selectors.h1).text() mustBe h1
    }

    "have the correct legend" in {
      doc.select(Selectors.legend(1)).text() mustBe h1
    }

    "have the correct text" in {
      doc.select(Selectors.p(1)).text() mustBe para
    }

    "have the correct bullets" in {
      doc.select(Selectors.bullet(1)).text() mustBe bullet1
      doc.select(Selectors.bullet(2)).text() mustBe bullet2
      doc.select(Selectors.bullet(3)).text() mustBe bullet3
    }
  }
}
