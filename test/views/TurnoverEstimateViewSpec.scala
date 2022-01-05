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

import forms.TurnoverEstimateFormProvider
import models.NormalMode
import views.html.turnoverEstimate

class TurnoverEstimateViewSpec extends ViewSpecBase {

  object Selectors extends BaseSelectors
  val view = app.injector.instanceOf[turnoverEstimate]

  implicit val msgs = messages
  val messageKeyPrefix = "turnoverEstimate"
  val h1 = "What do you think the business’s VAT-taxable turnover will be for the next 12 months?"
  val p1 = "Include the sale of all goods and services that are not exempt from VAT. You must include goods and services that have a 0% VAT rate."
  val estimateLinkText1 = "Find out more about"
  val estimateLinkText2 = "which goods and services are exempt from VAT (opens in new tab)."
  val estimateLink = "https://www.gov.uk/guidance/rates-of-vat-on-different-goods-and-services"
  val p2 = s"$estimateLinkText1 $estimateLinkText2"

  val form = new TurnoverEstimateFormProvider()()

  "TurnoverEstimate view" must {
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

    "have the first paragraph" in {
      doc.select(Selectors.p(1)).text() mustBe p1
    }

    "have the second paragraph with the correct url link" in {
      doc.select(Selectors.p(2)).select("a").attr("href") mustBe estimateLink
    }
  }
}