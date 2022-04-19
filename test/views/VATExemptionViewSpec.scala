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

import forms.VATExemptionFormProvider
import models.NormalMode
import views.html.vatExemption

class VATExemptionViewSpec extends ViewSpecBase {

  val view = app.injector.instanceOf[vatExemption]
  val messageKeyPrefix = "vatExemption"
  val form = new VATExemptionFormProvider()()

  val h1 = "Does the business want to apply for a VAT exemption?"
  val paragraph = "The business may not have to register for VAT if it sells mainly or only zero-rated goods or services."

  object Selectors extends BaseSelectors

  "VATExemption view" must {
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

    "have the first paragraph" in {
      doc.select(Selectors.p(1)).text() mustBe paragraph
    }

    "have the correct legend" in {
      doc.select(Selectors.legend(1)).text() mustBe h1
    }
  }
}
