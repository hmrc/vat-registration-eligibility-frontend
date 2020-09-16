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

import controllers.routes
import forms.AnnualAccountingSchemeFormProvider
import models.NormalMode
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import views.newbehaviours.YesNoViewBehaviours
import views.html.annualAccountingScheme

class AnnualAccountingSchemeViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "annualAccountingScheme"
  val form = new AnnualAccountingSchemeFormProvider()()
  implicit val msgs: Messages = messages

  val para = "This scheme is an option for companies with an estimated VAT-taxable turnover of £1.35 million or less."
  val bulletA = "Companies on the scheme:"
  val bulletA1 = "submit one VAT Return a year, rather than quarterly or monthly returns"
  val bulletA2 = "make monthly or quarterly payments, based on an HMRC estimate of their end-of-year VAT bill"
  val bulletB = "It may not suit companies that:"
  val bulletB1 = "want to keep up to date with the exact amount of VAT they owe or need to reclaim"
  val bulletB2 = "regularly reclaim more VAT than they charge, because they will only get one VAT refund a year"
  val linkText = "Annual Accounting Scheme (opens in a new tab)"
  val para2 = s"Find out more about the $linkText"
  val button = "Continue"

  object Selectors extends BaseSelectors

  def createView: () => HtmlFormat.Appendable =
    () => annualAccountingScheme(form, NormalMode)(fakeDataRequestIncorped, messages, frontendAppConfig)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable =
    (form: Form[_]) => annualAccountingScheme(form, NormalMode)(fakeDataRequestIncorped, messages, frontendAppConfig)

  "AnnualAccountingScheme view" must {
    behave like normalPage(createView(), messageKeyPrefix)
    behave like yesNoPage(
      form,
      createViewUsingForm,
      messageKeyPrefix,
      routes.AnnualAccountingSchemeController.onSubmit().url
    )

    val doc = asDocument(createViewUsingForm(form))

    "have a paragraph" in {
      doc.select(Selectors.p(1)).text() mustBe para
    }

    "have bullets with text" in {
      doc.select(Selectors.p(2)).text() mustBe bulletA
      doc.select(Selectors.bullet(1)).get(0).text() mustBe bulletA1
      doc.select(Selectors.bullet(2)).get(0).text() mustBe bulletA2

      doc.select(Selectors.p(3)).text() mustBe bulletB
      doc.select(Selectors.bullet(1)).get(1).text() mustBe bulletB1
      doc.select(Selectors.bullet(2)).get(1).text() mustBe bulletB2
    }

    "have a link" in {
      doc.select(Selectors.p(4)).text() mustBe para2
      doc.select(Selectors.link(1)).text() mustBe linkText
    }

    "have a continue button" in {
      val doc = asDocument(createViewUsingForm(form))
      doc.select(Selectors.button).text() mustBe button
    }
  }
}