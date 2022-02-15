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

package views

import forms.VATNumberFormProvider
import models.NormalMode
import views.html.VATNumber

class VATNumberViewSpec extends ViewSpecBase {

  object Selectors extends BaseSelectors
  object ExpectedContent {
    val h1Togc = "What is the VAT registration number of the business being taken over?"
    val text = "This is 9 numbers, for example 123456789. You can find it on the business VAT registration certificate."
  }

  val view = app.injector.instanceOf[VATNumber]
  val form = new VATNumberFormProvider()()

  "VatNumber view" must {
    lazy val doc = asDocument(view(form)(fakeDataRequestIncorped, messages, frontendAppConfig))

    "have the correct continue button" in {
      doc.select(Selectors.button).text() mustBe continueButton
    }

    "have the correct back link" in {
      doc.getElementById(Selectors.backLink).text() mustBe backLink
    }

    "have the correct heading" in {
      doc.select(Selectors.h1).text() mustBe ExpectedContent.h1Togc
    }

    "have the correct text" in {
      doc.select(Selectors.p(1)).text() mustBe ExpectedContent.text
    }
  }
}
