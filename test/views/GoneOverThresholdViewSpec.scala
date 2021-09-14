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

import forms.GoneOverThresholdFormProvider
import models.NormalMode
import views.html.GoneOverThreshold

class GoneOverThresholdViewSpec extends ViewSpecBase {
  object Selectors extends BaseSelectors
  val form = new GoneOverThresholdFormProvider()()
  val view = app.injector.instanceOf[GoneOverThreshold]

  object ExpectedContent {
    val heading        = "Has the business’s taxable turnover gone over £85,000 in any 12 month period?"
    val title          = s"$heading - Register for VAT - GOV.UK"
    val para           = "You must monitor your turnover every month and add up the total amount to cover the last 12 months. This is called a ‘rolling 12 month period’. If one month’s turnover takes you over £85,000 in any rolling 12 month period, you must register for VAT. "
    val detailsSummary = "What is taxable turnover?"
    val detailContent  = "VAT taxable turnover is the total value of everything you sell that is not exempt from VAT. Find out more about how to calculate your VAT taxable turnover (opens in new tab)"
  }

  "GoneOverThreshold view" must {
    val doc = asDocument(view(form, NormalMode)(fakeDataRequestIncorped, messages, frontendAppConfig))

    "have the correct back link" in {
      doc.getElementById(Selectors.backLink).text mustBe backLink
    }

    "have a correct title" in {
      doc.title() mustBe ExpectedContent.title
    }

    "have a correct heading" in {
      doc.select(Selectors.h1).text() mustBe ExpectedContent.heading
    }

    "have the correct legend" in {
      doc.select(Selectors.legend(1)).text() mustBe ExpectedContent.heading
    }

    "have a correct para" in {
      doc.select(Selectors.p(1)).text() mustBe ExpectedContent.para + ExpectedContent.detailContent
    }

    "have the correct details summary" in {
      doc.select(Selectors.detailsSummary).text() mustBe ExpectedContent.detailsSummary
    }

    "have the correct details content" in {
      doc.select(Selectors.detailsContent).text() mustBe ExpectedContent.detailContent
    }

    "have the correct continue button" in {
      doc.select(Selectors.button).text() mustBe continueButton
    }
  }
}
