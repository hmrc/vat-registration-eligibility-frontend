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

import forms.FixedEstablishmentFormProvider
import views.html.fixedEstablishment

class FixedEstablishmentViewSpec extends ViewSpecBase {

  val form = new FixedEstablishmentFormProvider()()
  implicit val msgs = messages
  val view = app.injector.instanceOf[fixedEstablishment]

  object Selectors extends BaseSelectors

  val h1 = "Does the business have a fixed establishment in the UK?"
  val paragraph = "A UK establishment exists if either the:"
  val bullet1 = "place where essential management decisions are made and the business’s central administration is carried out is in the UK"
  val bullet2 = "business has a permanent physical presence with the human and technical resources to make or receive taxable supplies in the UK"

  "FixedEstablishment view" must {
    lazy val doc = asDocument(view(form)(fakeDataRequestIncorped, messages, frontendAppConfig))

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

    "have the first paragraph" in {
      doc.select(Selectors.p(1)).text() mustBe paragraph
    }

    "have the correct legend" in {
      doc.select(Selectors.legend(1)).text() mustBe h1
    }

    "have bullets" in {
      doc.select(Selectors.bullet(1)).text() mustBe bullet1
      doc.select(Selectors.bullet(2)).text() mustBe bullet2
    }
  }
}
