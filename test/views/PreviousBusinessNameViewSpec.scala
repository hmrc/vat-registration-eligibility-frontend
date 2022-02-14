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

import forms.PreviousBusinessNameFormProvider
import models.NormalMode
import views.html.PreviousBusinessName

class PreviousBusinessNameViewSpec extends ViewSpecBase {

  object Selectors extends BaseSelectors

  val view = app.injector.instanceOf[PreviousBusinessName]

  implicit val msgs = messages
  val messageKeyPrefix = "previousBusinessName"
  val h1Togc = "What is the name of the previous business?"
  val h1Cole = "What was the business name prior to the change of legal entity?"
  val text = "If the business does not have a business name, enter the businesses trading name instead."

  val form = new PreviousBusinessNameFormProvider()()

  "PreviousBusinessName view" must {
    lazy val doc = asDocument(view(form, NormalMode)(fakeDataRequestIncorped, messages, frontendAppConfig))

    "have the correct continue button" in {
      doc.select(Selectors.button).text() mustBe continueButton
    }

    "have the correct back link" in {
      doc.getElementById(Selectors.backLink).text() mustBe backLink
    }

    "have the correct heading" in {
      doc.select(Selectors.h1).text() mustBe h1Togc
    }

    "have the correct text" in {
      doc.select(Selectors.p(1)).text() mustBe text
    }
  }
}