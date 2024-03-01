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

import views.html.MtdInformation

class MtdInformationViewSpec extends ViewSpecBase {

  object ExpectedContent {
    val h1 = "The business will be signed up for Making Tax Digital for VAT"
    val para = "Businesses signed up to Making Tax Digital for VAT must use software to:"
    val bullet1 = "keep digital records"
    val bullet2 = "submit VAT Returns directly to HMRC"
    val linkText = "Find out more about Making Tax Digital for VAT (opens in new tab)"
    val link = "https://www.gov.uk/guidance/making-tax-digital-for-vat"
    val buttonText = "Continue to register for VAT"
  }

  val view: MtdInformation = app.injector.instanceOf[MtdInformation]

  object Selectors extends BaseSelectors

  "Introduction view" must {
    lazy val doc = asDocument(view()(fakeRequest, messages, frontendAppConfig))

    "have the correct continue button" in {
      doc.select(Selectors.button).text() mustBe ExpectedContent.buttonText
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

    "have the correct first paragraph" in {
      doc.select(Selectors.p(1)).first.text mustBe ExpectedContent.para
    }

    "have the correct bullets" in {
      doc.select(Selectors.bullet(1)).text() mustBe ExpectedContent.bullet1
      doc.select(Selectors.bullet(2)).text() mustBe ExpectedContent.bullet2
    }

    "have the correct link" in {
      doc.select(Selectors.links).toList.head.text() mustBe ExpectedContent.linkText
      doc.select(Selectors.links).toList.head.attr("href") mustBe ExpectedContent.link
    }
  }
}
