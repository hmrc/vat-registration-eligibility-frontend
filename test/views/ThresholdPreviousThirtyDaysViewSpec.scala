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

import forms.ThresholdPreviousThirtyDaysFormProvider
import models.NormalMode
import play.api.data.Form
import play.api.i18n.Messages
import services.ThresholdService
import utils.TimeMachine
import views.html.ThresholdPreviousThirtyDays

import java.time.LocalDate

class ThresholdPreviousThirtyDaysViewSpec extends ViewSpecBase with ThresholdService {

  object TestTimeMachine extends TimeMachine {
    override def today: LocalDate = LocalDate.parse("2020-01-01")
  }

  val messageKeyPrefix = "thresholdPreviousThirtyDays"
  implicit val msgs: Messages = messages
  val form: Form[_] = new ThresholdPreviousThirtyDaysFormProvider(TestTimeMachine)(formattedVatThreshold())

  val h1Business = s"Did the business expect its taxable-turnover to go over $formattedVatThreshold in any 30 day period in the past?"
  val legendBusiness = "When did the business expect to go over the threshold?"
  val h1Partnership = s"Did the partnership expect its taxable-turnover to go over $formattedVatThreshold in any 30 day period in the past?"
  val legendPartnership = "When did the partnership expect to go over the threshold?"
  val paragraph = "This could happen if, for example, a business planned to run an exhibition and anticipated selling so many tickets it expected to go over the VAT threshold. The business must register for VAT when you expected it to go over the threshold, not when it actually went over the threshold."

  val view = app.injector.instanceOf[ThresholdPreviousThirtyDays]

  object Selectors extends BaseSelectors

  "ThresholdPreviousThirtyDays view" when {
    "Business entity is not partnership" must {
      lazy val doc = asDocument(view(form, NormalMode, vatThreshold = formattedVatThreshold())(fakeDataRequestIncorped, messages, frontendAppConfig))

      "have the correct continue button" in {
        doc.select(Selectors.button).text() mustBe continueButton
      }

      "have the correct back link" in {
        doc.select(Selectors.backLink).text() mustBe backLink
      }

      "have the correct browser title" in {
        doc.select(Selectors.title).first().text() mustBe title(h1Business)
      }

      "have the correct heading" in {
        doc.select(Selectors.h1).text() mustBe h1Business
      }

      "have the first paragraph" in {
        doc.select(Selectors.p(1)).text() mustBe paragraph
      }

      "have the correct legend" in {
        doc.select(Selectors.legend(2)).text() mustBe legendBusiness
      }
    }
    "Business entity is partnership" must {
      lazy val doc = asDocument(view(form, NormalMode, isPartnership = true, vatThreshold = formattedVatThreshold())(fakeDataRequestIncorped, messages, frontendAppConfig))

      "have the correct continue button" in {
        doc.select(Selectors.button).text() mustBe continueButton
      }

      "have the correct back link" in {
        doc.select(Selectors.backLink).text() mustBe backLink
      }

      "have the correct browser title" in {
        doc.select(Selectors.title).first().text() mustBe title(h1Partnership)
      }

      "have the correct heading" in {
        doc.select(Selectors.h1).text() mustBe h1Partnership
      }

      "have the first paragraph" in {
        doc.select(Selectors.p(1)).text() mustBe paragraph
      }

      "have the correct legend" in {
        doc.select(Selectors.legend(2)).text() mustBe legendPartnership
      }
    }
  }

}