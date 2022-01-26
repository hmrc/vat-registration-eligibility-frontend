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

import forms.ThresholdInTwelveMonthsFormProvider
import models.NormalMode
import utils.TimeMachine
import views.html.thresholdInTwelveMonths

import java.time.LocalDate


class ThresholdInTwelveMonthsViewSpec extends ViewSpecBase {

  object TestTimeMachine extends TimeMachine {
    override def today: LocalDate = LocalDate.parse("2021-01-01")
  }

  val form = new ThresholdInTwelveMonthsFormProvider(TestTimeMachine)()
  val view = app.injector.instanceOf[thresholdInTwelveMonths]

  object ExpectedContent {
    val h1Business = "Has the business’s taxable turnover gone over £85,000 in any 12 month period?"
    val h1Partnership = "Has the partnership’s taxable turnover gone over £85,000 in any 12 month period?"
    val para1 = "VAT taxable turnover is the total value of everything sold that is not exempt from VAT. "
    val para2 = "Find out more about how to calculate VAT taxable turnover (opens in new tab)"
    val paragraph = "The business must monitor its turnover every month and add up the total amount to cover the last 12 months. This is called a ‘rolling 12 month period’. If one month’s turnover takes the business over £85,000 in any rolling 12 month period, it must register for VAT. " + para1 + para2
    val detailsHeading = "What is taxable turnover?"
    val bullet1 = "Yes"
    val bullet2 = "No"
    val h2Business = "Has the business’s taxable turnover gone over £85,000 in any 12 month period? When did the business go over the threshold?"
    val h2Partnership = "Has the partnership’s taxable turnover gone over £85,000 in any 12 month period? When did the partnership go over the threshold?"
    val button = "Continue"
  }

  "ThresholdInTwelveMonths view" when {
    "Business entity is not partnership" must {
      object Selectors extends BaseSelectors
      val doc = asDocument(view(form, NormalMode)(fakeDataRequestIncorped, messages, frontendAppConfig))

      "have a heading" in {
        doc.select(Selectors.h1).text() mustBe ExpectedContent.h1Business
      }

      "have the correct back link" in {
        doc.getElementById(Selectors.backLink).text() mustBe backLink
      }

      "have the correct browser title" in {
        doc.select(Selectors.title).text() mustBe title(ExpectedContent.h1Business)
      }

      "have a paragraph" in {
        doc.select(Selectors.p(1)).text() mustBe ExpectedContent.paragraph
      }

      "have a dropdown heading" in {
        doc.select(Selectors.detailsSummary).text() mustBe ExpectedContent.detailsHeading
      }

      "have a continue button" in {
        doc.select(Selectors.button).text() mustBe ExpectedContent.button
      }

      "contain a legend for the question" in {
        doc.select(Selectors.legend(1)).text() mustBe ExpectedContent.h2Business
      }
    }
    "Business entity is partnership" must {
      object Selectors extends BaseSelectors
      val doc = asDocument(view(form, NormalMode, isPartnership = true)(fakeDataRequestIncorped, messages, frontendAppConfig))

      "have a heading" in {
        doc.select(Selectors.h1).text() mustBe ExpectedContent.h1Partnership
      }

      "have the correct back link" in {
        doc.getElementById(Selectors.backLink).text() mustBe backLink
      }

      "have the correct browser title" in {
        doc.select(Selectors.title).text() mustBe title(ExpectedContent.h1Partnership)
      }

      "have a paragraph" in {
        doc.select(Selectors.p(1)).text() mustBe ExpectedContent.paragraph
      }

      "have a dropdown heading" in {
        doc.select(Selectors.detailsSummary).text() mustBe ExpectedContent.detailsHeading
      }

      "have a continue button" in {
        doc.select(Selectors.button).text() mustBe ExpectedContent.button
      }

      "contain a legend for the question" in {
        doc.select(Selectors.legend(1)).text() mustBe ExpectedContent.h2Partnership
      }
    }
  }
}
