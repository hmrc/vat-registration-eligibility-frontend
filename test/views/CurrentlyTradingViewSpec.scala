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

import forms.CurrentlyTradingFormProvider
import models.NormalMode
import views.html.CurrentlyTrading

class CurrentlyTradingViewSpec extends ViewSpecBase {

  val messageKeyPrefix = "currentlyTrading"
  val form = new CurrentlyTradingFormProvider()()

  val h1Business = "Is the business currently trading taxable goods?"
  val h1Partnership = "Is the partnership currently trading taxable goods?"
  val yesText = "Yes"
  val noText = "No"

  val view: CurrentlyTrading = app.injector.instanceOf[CurrentlyTrading]

  object Selectors extends BaseSelectors

  "CurrentlyTrading view" when {
    "Business entity is not partnership" must {
      lazy val doc = asDocument(view(form, NormalMode)(fakeDataRequest, messages, frontendAppConfig))

      "have the correct continue button" in {
        doc.select(Selectors.button).text() mustBe continueButton
      }

      "have the correct back link" in {
        doc.select(Selectors.backLink).text() mustBe backLink
      }

      "have the correct browser title" in {
        doc.select(Selectors.title).text() mustBe title(h1Business)
      }

      "have the correct heading" in {
        doc.select(Selectors.h1).text() mustBe h1Business
      }

      "have the correct legend" in {
        doc.select(Selectors.legend(1)).text() mustBe h1Business
      }

      "have the correct radio text" in {
        doc.select(Selectors.radioYes).text() mustBe yesText
        doc.select(Selectors.radioNo).text() mustBe noText
      }
    }

    "Business entity is partnership" must {
      lazy val doc = asDocument(view(form, NormalMode, isPartnership = true)(fakeDataRequest, messages, frontendAppConfig))

      "have the correct continue button" in {
        doc.select(Selectors.button).text() mustBe continueButton
      }

      "have the correct back link" in {
        doc.select(Selectors.backLink).text() mustBe backLink
      }

      "have the correct browser title" in {
        doc.select(Selectors.title).text() mustBe title(h1Partnership)
      }

      "have the correct heading" in {
        doc.select(Selectors.h1).text() mustBe h1Partnership
      }

      "have the correct legend" in {
        doc.select(Selectors.legend(1)).text() mustBe h1Partnership
      }

      "have the correct radio text" in {
        doc.select(Selectors.radioYes).text() mustBe yesText
        doc.select(Selectors.radioNo).text() mustBe noText
      }
    }
  }

}
