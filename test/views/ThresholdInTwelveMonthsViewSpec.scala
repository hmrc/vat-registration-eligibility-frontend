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

import java.time.LocalDate

import config.FrontendAppConfig
import forms.ThresholdInTwelveMonthsFormProvider
import javax.inject.Inject
import models.NormalMode
import services.ThresholdService
import utils.TimeMachine
import views.html.thresholdInTwelveMonths


class ThresholdInTwelveMonthsViewSpec extends ViewSpecBase {

  object TestTimeMachine extends TimeMachine {
    override def today: LocalDate = LocalDate.parse("2021-01-01")
  }

  val messageKeyPrefix = "thresholdInTwelveMonths"
  val form = new ThresholdInTwelveMonthsFormProvider(TestTimeMachine)()

  val thresholdService: ThresholdService = app.injector.instanceOf[ThresholdService]

  val h1 = "Has the business’s taxable turnover gone over £85,000 in any 12 month period?"
  val paragraph = "You must monitor your turnover every month and add up the total amount to cover the last 12 months. This is called a ‘rolling 12 month period’. If one month’s turnover takes you over £85,000 in any rolling 12 month period, you must register for VAT."
  val bullet1 = "Yes"
  val bullet2 = "No"
  val h2 = "Has the business’s taxable turnover gone over £85,000 in any 12 month period? When did the business go over the threshold?"
  val button = "Continue"

  object Selectors extends BaseSelectors

  "ThresholdInTwelveMonths view" must {
    val doc = asDocument(thresholdInTwelveMonths(form, NormalMode, thresholdService)(fakeDataRequestIncorped, messages, frontendAppConfig))

    "have a heading" in {
      doc.select(Selectors.h1).text() mustBe h1
    }

    "have the correct back link" in {
      doc.getElementById(Selectors.backLink).text() mustBe backLink
    }

    "have the correct browser title" in {
      doc.select(Selectors.title).text() mustBe title(h1)
    }

    "have a paragraph" in {
      doc.select(Selectors.p(1)).text() mustBe paragraph
    }

    "have a continue button" in {
      doc.select(Selectors.button).text() mustBe button
    }

    "contain a legend for the question" in {
      doc.select(Selectors.legend(1)).text() mustBe h2
    }
  }
}
