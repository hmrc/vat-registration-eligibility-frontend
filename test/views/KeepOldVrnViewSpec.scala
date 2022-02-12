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

import forms.KeepOldVrnFormProvider
import views.html.KeepOldVrn

class KeepOldVrnViewSpec extends ViewSpecBase {

  val messageKeyPrefix = "keepOldVrn"
  val form = new KeepOldVrnFormProvider()()

  val h1 = "Do you want to keep the existing VAT registration number?"

  val view = app.injector.instanceOf[KeepOldVrn]

  object Selectors extends BaseSelectors

  "Keep VAT Number view" must {
    lazy val doc = asDocument(view(form)(fakeDataRequest, messages, frontendAppConfig))

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
  }
}
