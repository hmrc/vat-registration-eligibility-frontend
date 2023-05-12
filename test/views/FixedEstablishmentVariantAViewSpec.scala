/*
 * Copyright 2023 HM Revenue & Customs
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
import views.html.fixed_establishment_variant_a

class FixedEstablishmentVariantAViewSpec extends ViewSpecBase {

  val form = new FixedEstablishmentFormProvider()()
  implicit val msgs = messages
  val view = app.injector.instanceOf[fixed_establishment_variant_a]

  object Selectors extends BaseSelectors

  "fixed_establishment_variant_a view" must {
    lazy val doc = asDocument(view(form)(fakeDataRequestIncorped, messages, frontendAppConfig))

    "have the correct continue button" in {
      doc.select(Selectors.button).text() mustBe continueButton
    }

    "have the correct back link" in {
      doc.select(Selectors.backLink).text() mustBe backLink
    }

    "have the correct browser title" in {
      doc.select(Selectors.title).text() mustBe "Does the business have a fixed establishment in the UK?"
    }

    "have the correct heading" in {
      doc.select(Selectors.h1).text() mustBe "Business establishment"
    }

    "have the first paragraph" in {
      doc.select(Selectors.p(1)).text() mustBe "We need to know where your business has a fixed establishment."
    }

    "have the second paragraph" in {
      doc.select(Selectors.p(2)).text() mustBe "A fixed establishment exists if either the:"
    }

    "have the correct legend" in {
      doc.select(Selectors.legend(1)).text() mustBe "Does the business have a fixed establishment in the UK?"
    }

    "have bullets" in {
      doc.select(Selectors.bullet(1)).text() mustBe "place where essential management decisions are made and the business’s central administration is carried out is in the UK or Isle of Man"
      doc.select(Selectors.bullet(2)).text() mustBe "business has a permanent physical presence with the human and technical resources to make or receive taxable supplies in the UK or Isle of Man"
    }

    "have the correct inset text" in {
      doc.select(".govuk-inset-text").text() mustBe "The Isle of Man is treated as part of the UK for VAT purposes."
    }
  }
}

