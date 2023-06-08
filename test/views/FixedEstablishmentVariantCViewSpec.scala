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
import play.twirl.api.HtmlFormat
import views.html.{VariantLayout, fixed_establishment_variant_c}

class FixedEstablishmentVariantCViewSpec extends ViewSpecBase {

  implicit val msgs = messages
  implicit val dataReq = fakeDataRequest

  val form = new FixedEstablishmentFormProvider()()
  val variantLayout = app.injector.instanceOf[VariantLayout]
  val fixedEstablishmentView: fixed_establishment_variant_c = app.injector.instanceOf[fixed_establishment_variant_c]
  val fixedEstablishmentViewHtml: HtmlFormat.Appendable = fixedEstablishmentView(form)
  val view = variantLayout(fixedEstablishmentViewHtml)

  object Selectors extends BaseSelectors

  val h1 = "Does the business have a fixed establishment in the UK or Isle of Man?"
  val paragraph = "A UK or Isle of Man establishment exists if either the:"
  val bullet1 = "place where essential management decisions are made and the business’s central administration is carried out is in the UK or Isle of Man"
  val bullet2 = "business has a permanent physical presence with the human and technical resources to make or receive taxable supplies in the UK or Isle of Man"

  "FixedEstablishment view" must {
    lazy val doc = asDocument(view)

    "have the correct continue button" in {
      doc.select(Selectors.button).text() mustBe continueButton
    }

    "have the correct back link" in {
      doc.select(Selectors.backLink).text() mustBe backLink
    }

    "have the correct heading" in {
      doc.select("h1").text() mustBe "Does the business have a fixed establishment in the UK?"
    }

    "have the first paragraph" in {
      doc.select("#p1").text() mustBe "The Isle of Man is treated as part of the UK for VAT purposes."
    }

    "have a detail" in {
      doc.select(".govuk-details__summary-text").text() mustBe "What is a fixed establishment?"
    }

    "have a detail paragraph" in {
      doc.select("#p2").text() mustBe "A fixed establishment exists if either the:"
    }

    "have bullets" in {
      doc.select(".govuk-details__text > ul > li:nth-of-type(1)").text() mustBe "place where essential management decisions are made and the business’s central administration is carried out"
      doc.select(".govuk-details__text > ul > li:nth-of-type(2)").text() mustBe "business has a permanent physical presence with the human and technical resources to make or receive taxable supplies"
    }
  }
}

