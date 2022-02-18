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

import views.html.TermsAndConditions

class TermsAndConditionsViewSpec extends ViewSpecBase {

  val messageKeyPrefix = "termsAndConditions"

  val h1 = "Terms and conditions"
  val para = "By choosing to keep its current VAT registration number, the business agrees:"
  val bullet1 = "to submit its first VAT return to HMRC, with the VAT due for the whole period on the form"
  val bullet2 = "to submit any of the previous owner’s outstanding VAT returns"
  val bullet3 = "to pay HMRC any VAT due on the previous owner’s stocks, assets and any supplies they made"
  val bullet4 = "that any VAT return the previous owner made after the transfer date, will be treated as though made by the current business"
  val bullet5 = "it will have no right to claim money HMRC paid the previous owner before the transfer of the business"
  val button = "Accept and continue"

  val view = app.injector.instanceOf[TermsAndConditions]

  object Selectors extends BaseSelectors

  "TermsAndConditions view" must {
    lazy val doc = asDocument(view()(fakeRequest, messages, frontendAppConfig))

    "have the correct browser title" in {
      doc.select(Selectors.title).text() mustBe title(h1)
    }

    "have the correct back link" in {
      doc.getElementById(Selectors.backLink).text() mustBe backLink
    }

    "have the correct heading" in {
      doc.select(Selectors.h1).text() mustBe h1
    }

    "have the correct paragraph" in {
      doc.select(Selectors.p(1)).text() mustBe para
    }

    "have bullets with the correct text" in {
      doc.select(Selectors.bullet(1)).get(0).text() mustBe bullet1
      doc.select(Selectors.bullet(2)).get(0).text() mustBe bullet2
      doc.select(Selectors.bullet(3)).get(0).text() mustBe bullet3
      doc.select(Selectors.bullet(4)).get(0).text() mustBe bullet4
      doc.select(Selectors.bullet(5)).get(0).text() mustBe bullet5
    }
  }
}
