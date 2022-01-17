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

import forms.RegistrationReasonFormProvider
import models.NormalMode
import views.html.RegistrationReasonView

class RegistrationReasonViewSpec extends ViewSpecBase {
  object Selectors extends BaseSelectors
  val form = new RegistrationReasonFormProvider()()
  val view = app.injector.instanceOf[RegistrationReasonView]

  object ExpectedContent {
    val headingBusiness = "Why do you want to register the business for VAT?"
    val titleBusiness   = s"$headingBusiness - Register for VAT - GOV.UK"
    val headingPartnership = "Why do you want to register the partnership for VAT?"
    val titlePartnership   = s"$headingPartnership - Register for VAT - GOV.UK"
    val radio1  = "It’s selling goods or services and needs or wants to charge VAT to customers"
    val radio2  = "It’s taking over a VAT registered business as a Transfer of a Going Concern"
    val radio3  = "You’re changing the legal entity of the business (for example, from sole trader to limited company)"
    val radio4  = "You’re setting up a VAT group"
    val radio5  = "It’s a UK established overseas exporter"
    val hint    = "The business is established within the UK and will only make taxable supplies outside of the UK."
    val error   = "Select the reason you want to register the business for VAT"
  }

  "RegistrationReason view" when {
    "Business entity is not partnership" must {
      val doc = asDocument(view(form, NormalMode)(fakeDataRequestIncorped, messages, frontendAppConfig))

      "have the correct back link" in {
        doc.getElementById(Selectors.backLink).text mustBe backLink
      }

      "have a correct title" in {
        doc.title() mustBe ExpectedContent.titleBusiness
      }

      "have a correct heading" in {
        doc.select(Selectors.h1).text() mustBe ExpectedContent.headingBusiness
      }

      "have the right radio options" in {
        doc.select(Selectors.radio(1)).text() mustBe ExpectedContent.radio1
        doc.select(Selectors.radio(2)).text() mustBe ExpectedContent.radio5
      }

      "have the right hint" in {
        doc.select(Selectors.hint).text() mustBe ExpectedContent.hint
      }

      "have the correct continue button" in {
        doc.select(Selectors.button).text() mustBe continueButton
      }
    }
    "Business entity is partnership " must {
      val doc = asDocument(view(form, NormalMode, isPartnership = true)(fakeDataRequestIncorped, messages, frontendAppConfig))

      "have the correct back link" in {
        doc.getElementById(Selectors.backLink).text mustBe backLink
      }

      "have a correct title" in {
        doc.title() mustBe ExpectedContent.titlePartnership
      }

      "have a correct heading" in {
        doc.select(Selectors.h1).text() mustBe ExpectedContent.headingPartnership
      }

      "have the right radio options" in {
        doc.select(Selectors.radio(1)).text() mustBe ExpectedContent.radio1
        doc.select(Selectors.radio(2)).text() mustBe ExpectedContent.radio5
      }

      "have the right hint" in {
        doc.select(Selectors.hint).text() mustBe ExpectedContent.hint
      }

      "have the correct continue button" in {
        doc.select(Selectors.button).text() mustBe continueButton
      }
    }
    "VAT group flow is enabled" must {
      val doc = asDocument(view(form, NormalMode, isVatGroupFlow = true)(fakeDataRequestIncorped, messages, frontendAppConfig))

      "have the right radio options" in {
        doc.select(Selectors.radio(1)).text() mustBe ExpectedContent.radio1
        doc.select(Selectors.radio(2)).text() mustBe ExpectedContent.radio5
        doc.select(Selectors.radio(3)).text() mustBe ExpectedContent.radio4
      }

    }
  }
  
}
