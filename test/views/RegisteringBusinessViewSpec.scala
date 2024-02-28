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

import forms.RegisteringBusinessFormProvider
import models.NormalMode
import utils.JsoupElementExtractor
import views.html.RegisteringBusinessView

class RegisteringBusinessViewSpec extends ViewSpecBase with JsoupElementExtractor {

  val messageKeyPrefix = "registeringBusiness"
  val form = new RegisteringBusinessFormProvider()()
  val view = app.injector.instanceOf[RegisteringBusinessView]

  object Selectors extends BaseSelectors

  object ExpectedContent {
    val h1 = "Whose business do you want to register?"
    val ownOption = "Your own"
    val ownOptionHint = "Select this option if you are the owner or proprietor of the business you wish to register."
    val someoneElseOption = "Someone else’s"
    val someoneElseOptionHint = "Select this option if you are an employee of the business you are registering or were otherwise asked to complete this registration on behalf of someone else."
  }

  "RegisteringBusiness view" must {
    lazy val doc = asDocument(view(form, NormalMode)(fakeDataRequestIncorped, messages, frontendAppConfig))

    "have the correct continue button" in {
      doc.select(Selectors.button).text() mustBe continueButton
    }

    "have the correct back link" in {
      doc.select(Selectors.backLink).text() mustBe backLink
    }

    "have the correct browser title" in {
      doc.select(Selectors.title).text() mustBe title(ExpectedContent.h1)
    }

    "have the correct heading" in {
      doc.select(Selectors.h1).text() mustBe ExpectedContent.h1
    }

    "have the correct legend" in {
      doc.select(Selectors.legend(1)).text() mustBe ExpectedContent.h1
    }

    "have the correct radio options" in {
      doc.select(Selectors.radio(1)).text() mustBe ExpectedContent.ownOption
      doc.select(Selectors.radio(2)).text() mustBe ExpectedContent.someoneElseOption
    }

    "have the correct hint text for Your own option" in {
      doc.select(Selectors.hint).getTextContent(1) mustBe Some(ExpectedContent.ownOptionHint)
    }

    "have the correct hint text for Someone else option" in {
      doc.select(Selectors.hint).getTextContent(2) mustBe Some(ExpectedContent.someoneElseOptionHint)
    }
  }
}
