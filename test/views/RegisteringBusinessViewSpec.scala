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

import forms.RegisteringBusinessFormProvider
import models.NormalMode
import views.html.registeringBusiness

class RegisteringBusinessViewSpec extends ViewSpecBase {

  val messageKeyPrefix = "registeringBusiness"
  val form = new RegisteringBusinessFormProvider()()
  val view = app.injector.instanceOf[registeringBusiness]

  object Selectors extends BaseSelectors

  object ExpectedContent {
    val h1 = "Whose business do you want to register?"
    val ownOption = "Your own"
    val someoneElseOption = "Someone else’s"
  }

  "RegisteringBusiness view" must {
    lazy val doc = asDocument(view(form, NormalMode)(fakeDataRequestIncorped, messages, frontendAppConfig))

    "have the correct continue button" in {
      doc.select(Selectors.button).text() mustBe continueButton
    }

    "have the correct back link" in {
      doc.getElementById(Selectors.backLink).text() mustBe backLink
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
  }
}
