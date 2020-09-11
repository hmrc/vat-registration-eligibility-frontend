/*
 * Copyright 2020 HM Revenue & Customs
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

import forms.ThresholdNextThirtyDaysFormProvider
import models.NormalMode
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import utils.TimeMachine
import views.html.thresholdNextThirtyDays
import views.newbehaviours.ViewBehaviours

class ThresholdNextThirtyDaysViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "thresholdNextThirtyDays"

  object TestTimeMachine extends TimeMachine {
    override def today: LocalDate = LocalDate.parse("2020-01-01")
  }

  object Selectors extends BaseSelectors

  implicit val msgs: Messages = messages
  val form = new ThresholdNextThirtyDaysFormProvider(TestTimeMachine)()

  def createView: () => HtmlFormat.Appendable = () =>
    thresholdNextThirtyDays(form, NormalMode)(fakeDataRequestIncorped, messages, frontendAppConfig)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    thresholdNextThirtyDays(form, NormalMode)(fakeDataRequestIncorped, messages, frontendAppConfig)

  val testText = "For example, the business is expecting orders - or has signed a contract - that will generate VAT-taxable sales of £85,000 or more in the next 30 days."
  val testLegend = "What date did the business realise it would go over the threshold?"
  val testHint = "For example, 13 02 2017"
  val testButton = "Save and continue"

  "ThresholdNextThirtyDays view" must {
    behave like normalPage(createView(), messageKeyPrefix)
    behave like pageWithBackLink(createView())
    behave like pageWithSubmitButton(createView(), testButton)

    val doc = asDocument(createViewUsingForm(form))

    "have the correct text" in {
      doc.select(Selectors.p(1)).text() mustBe testText
    }

    "have the correct legend" in {
      doc.select(Selectors.legend(2)).text() mustBe testLegend
    }

    "have the correct hint" in {
      doc.select(Selectors.hint).text() mustBe testHint
    }
  }
}
