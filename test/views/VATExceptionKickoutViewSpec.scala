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

import forms.VATExceptionKickoutFormProvider
import models.NormalMode
import views.html.vatExceptionKickout

class VATExceptionKickoutViewSpec extends ViewSpecBase {
  val view = app.injector.instanceOf[vatExceptionKickout]
  val messageKeyPrefix = "vatRegistrationException"
  val form = new VATExceptionKickoutFormProvider()()

  val h1 = "You can ask for an exception if you have only temporarily gone over the VAT threshold"
  val paragraph = "An exception means you will not have to register for VAT if you can provide evidence that your VAT taxable turnover will not go over the deregistration threshold of £83,000 in the next 12 months. To ask for an exception, you will need to write to HMRC."
  val yesText = "I want to ask for an exception"
  val noText = "I do not want to ask for an exception"

  object Selectors extends BaseSelectors

  "VATRegistrationException view" must {
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

    "have the correct radio text" in {
      doc.select(Selectors.radioYes).text() mustBe yesText
      doc.select(Selectors.radioNo).text() mustBe noText
    }
  }
}
