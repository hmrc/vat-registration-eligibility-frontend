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

import forms.AgriculturalFlatRateSchemeFormProvider
import models.NormalMode
import views.html.agriculturalFlatRateScheme

class AgriculturalFlatRateSchemeViewSpec extends ViewSpecBase {

  val messageKeyPrefix = "agriculturalFlatRateScheme"
  val form = new AgriculturalFlatRateSchemeFormProvider()()

  val h1Business = "Is the business applying for the Agricultural Flat Rate Scheme?"
  val h1Partnership = "Is the partnership applying for the Agricultural Flat Rate Scheme?"
  val p1 = "The scheme is a different type of VAT registration for farmers."
  val afrsLinkText1 = "Find out more about the"
  val afrsLinkText2 = "Agricultural Flat Rate Scheme (opens in a new window)."
  val afrsLink = "https://www.gov.uk/government/publications/vat-notice-70046-agricultural-flat-rate-scheme/vat-notice-70046-agricultural-flat-rate-scheme"
  val p2 = s"$afrsLinkText1 $afrsLinkText2"

  object Selectors extends BaseSelectors

  val view = app.injector.instanceOf[agriculturalFlatRateScheme]

  "AgriculturalFlatRateScheme view" when {
    
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

      "have the first paragraph" in {
        doc.select(Selectors.p(1)).text() mustBe p1
      }

      "have the correct legend" in {
        doc.select(Selectors.legend(1)).text() mustBe h1Business
      }

      "have the second paragraph with the correct url link" in {
        doc.select(Selectors.p(2)).select("a").attr("href") mustBe afrsLink
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

      "have the first paragraph" in {
        doc.select(Selectors.p(1)).text() mustBe p1
      }

      "have the correct legend" in {
        doc.select(Selectors.legend(1)).text() mustBe h1Partnership
      }

      "have the second paragraph with the correct url link" in {
        doc.select(Selectors.p(2)).select("a").attr("href") mustBe afrsLink
      }
    }
  }
}