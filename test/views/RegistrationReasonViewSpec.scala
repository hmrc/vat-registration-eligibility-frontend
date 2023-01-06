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

import forms.RegistrationReasonFormProvider
import models.NormalMode
import utils.JsoupElementExtractor
import views.html.RegistrationReasonView

class RegistrationReasonViewSpec extends ViewSpecBase with JsoupElementExtractor {
  object Selectors extends BaseSelectors

  val form = new RegistrationReasonFormProvider()()
  val view = app.injector.instanceOf[RegistrationReasonView]

  object ExpectedContent {
    val headingBusiness = "Why do you want to register the business for VAT?"
    val titleBusiness = s"$headingBusiness - Register for VAT - GOV.UK"
    val headingPartnership = "Why do you want to register the partnership for VAT?"
    val titlePartnership = s"$headingPartnership - Register for VAT - GOV.UK"
    val radio1 = "It’s selling goods or services and needs or wants to charge VAT to customers"
    val radio2 = "It’s taking over a VAT registered business as a Transfer of a Going Concern"
    val radio3 = "You’re changing the legal entity of the business (for example, from sole trader to limited company)"
    val radio4 = "You’re setting up a VAT group"
    val hint1 = "A group of businesses treated as one entity for VAT purposes."
    val radio5 = "It’s a UK established overseas exporter"
    val hint2 = "The business is established within the UK and will only make taxable supplies outside of the UK."
    val error = "Select the reason you want to register the business for VAT"
  }

  "RegistrationReason view" when {
    "Business entity is not partnership" must {
      val doc = asDocument(view(form, NormalMode, showVatGroup = true, isOverseas = false)(fakeDataRequestIncorped, messages, frontendAppConfig))

      "have the correct back link" in {
        doc.select(Selectors.backLink).text mustBe backLink
      }

      "have a correct title" in {
        doc.title() mustBe ExpectedContent.titleBusiness
      }

      "have a correct heading" in {
        doc.select(Selectors.h1).text() mustBe ExpectedContent.headingBusiness
      }

      "have the right radio options" in {
        doc.select(Selectors.radio(1)).text() mustBe ExpectedContent.radio1
        doc.select(Selectors.radio(2)).text() mustBe ExpectedContent.radio2
        doc.select(Selectors.radio(3)).text() mustBe ExpectedContent.radio3
        doc.select(Selectors.radio(4)).text() mustBe ExpectedContent.radio4
        doc.select(Selectors.radio(5)).text() mustBe ExpectedContent.radio5
      }

      "have the right radio options for an overseas user" in {
        val doc = asDocument(view(form, NormalMode, showVatGroup = false, isOverseas = true)(fakeDataRequestIncorped, messages, frontendAppConfig))

        doc.select(Selectors.radio(1)).text() mustBe ExpectedContent.radio1
        doc.select(Selectors.radio(2)).text() mustBe ExpectedContent.radio2
        doc.select(Selectors.radio(3)).text() mustBe ExpectedContent.radio3
      }

      "have the correct hint text for Setting up VAT Group option" in {
        doc.select(Selectors.hint).getTextContent(1) mustBe Some(ExpectedContent.hint1)
      }

      "have the correct hint text for UK established overseas exporter option" in {
        doc.select(Selectors.hint).getTextContent(2) mustBe Some(ExpectedContent.hint2)
      }

      "have the correct continue button" in {
        doc.select(Selectors.button).text() mustBe continueButton
      }
    }
    "Business entity is partnership " must {
      val doc = asDocument(view(form, NormalMode, isPartnership = true)(fakeDataRequestIncorped, messages, frontendAppConfig))

      "have the correct back link" in {
        doc.select(Selectors.backLink).text mustBe backLink
      }

      "have a correct title" in {
        doc.title() mustBe ExpectedContent.titlePartnership
      }

      "have a correct heading" in {
        doc.select(Selectors.h1).text() mustBe ExpectedContent.headingPartnership
      }

      "have the right radio options" in {
        doc.select(Selectors.radio(1)).text() mustBe ExpectedContent.radio1
        doc.select(Selectors.radio(2)).text() mustBe ExpectedContent.radio2
        doc.select(Selectors.radio(3)).text() mustBe ExpectedContent.radio3
        doc.select(Selectors.radio(4)).text() mustBe ExpectedContent.radio5
      }

      "have the right radio options for an overseas user" in {
        val doc = asDocument(view(form, NormalMode, showVatGroup = false, isOverseas = true, isPartnership = true)(fakeDataRequestIncorped, messages, frontendAppConfig))

        doc.select(Selectors.radio(1)).text() mustBe ExpectedContent.radio1
        doc.select(Selectors.radio(2)).text() mustBe ExpectedContent.radio2
        doc.select(Selectors.radio(3)).text() mustBe ExpectedContent.radio3
      }

      "have the correct hint text for UK established overseas exporter option" in {
        doc.select(Selectors.hint).getTextContent(1) mustBe Some(ExpectedContent.hint2)
      }

      "have the correct continue button" in {
        doc.select(Selectors.button).text() mustBe continueButton
      }
    }
  }

}
