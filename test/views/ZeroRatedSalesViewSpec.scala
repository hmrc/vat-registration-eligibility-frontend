/*
 * Copyright 2021 HM Revenue & Customs
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

import forms.ZeroRatedSalesFormProvider
import models.NormalMode
import views.html.zeroRatedSales

class ZeroRatedSalesViewSpec extends ViewSpecBase {
  val messageKeyPrefix = "zeroRatedSales"
  val form = new ZeroRatedSalesFormProvider()()

  val h1 = "Does the business sell mainly zero-rated goods or services?"
  val detailsSummary = "Examples of zero-rated goods or services"
  val linkText = "VAT rates on different goods and services (opens in new tab)"
  val line1 = "Zero-rated goods and services are VAT-taxable but the VAT rate on them is 0%."
  val line2 = "They include:"
  val line3 = s"Find out about $linkText"
  val bullet1 = "most food and drink (but not things like alcoholic drinks, confectionery, crisps and savoury snacks, hot food, sports drinks, hot takeaways, ice cream, soft drinks and mineral water)"
  val bullet2 = "books and newspapers"
  val bullet3 = "printing services for brochures, leaflets or pamphlets"
  val bullet4 = "children’s clothes and shoes"
  val bullet5 = "most goods you export to non-EU countries"

  val view = app.injector.instanceOf[zeroRatedSales]

  object Selectors extends BaseSelectors

  "ZeroRatedSales view" must {
    lazy val doc = asDocument(view(form, NormalMode)(fakeDataRequestIncorped, messages, frontendAppConfig))

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

    "have the correct legend" in {
      doc.select(Selectors.legend(1)).text() mustBe h1
    }

    "have a dropdown summary" in {
      doc.select(Selectors.detailsSummary).text() mustBe detailsSummary
    }

    "have the right paragraphs" in {
      doc.select(Selectors.p(1)).text() mustBe line1
      doc.select(Selectors.p(2)).text() mustBe line2
      doc.select(Selectors.p(4)).text() mustBe line3
    }

    "have the right bullets" in {
      doc.select(Selectors.bullet(1)).text() mustBe bullet1
      doc.select(Selectors.bullet(2)).text() mustBe bullet2
      doc.select(Selectors.bullet(3)).text() mustBe bullet3
      doc.select(Selectors.bullet(4)).text() mustBe bullet4
      doc.select(Selectors.bullet(5)).text() mustBe bullet5
    }
  }
}
