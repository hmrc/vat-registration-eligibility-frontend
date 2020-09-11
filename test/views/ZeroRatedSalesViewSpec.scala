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
import deprecated.DeprecatedConstants
import forms.ZeroRatedSalesFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.zeroRatedSales
import views.newbehaviours.YesNoViewBehaviours

class ZeroRatedSalesViewSpec extends YesNoViewBehaviours {
  val messageKeyPrefix = "zeroRatedSales"
  val form = new ZeroRatedSalesFormProvider()()
  implicit val msgs = messages

  val detailsSummary = "Examples of zero-rated goods or services"
  val linkText = "VAT rates on different goods and services (opens in new window)"
  val line1 = "Zero-rated goods and services are VAT-taxable but the VAT rate on them is 0%."
  val line2 = "They include:"
  val line3 = s"Find out about $linkText."
  val bullet1 = "most food and drink (but not things like alcoholic drinks, confectionery, crisps and savoury snacks, hot food, sports drinks, hot takeaways, ice cream, soft drinks and mineral water)"
  val bullet2 = "books and newspapers"
  val bullet3 = "printing services for brochures, leaflets or pamphlets"
  val bullet4 = "children's clothes and shoes"
  val bullet5 = "most goods you export to non-EU countries"

  object Selectors extends BaseSelectors

  def createView: () => HtmlFormat.Appendable =
    () => zeroRatedSales(form, NormalMode)(fakeDataRequestIncorped, messages, frontendAppConfig)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable =
    (form: Form[_]) => zeroRatedSales(form, NormalMode)(fakeDataRequestIncorped, messages, frontendAppConfig)

  "ZeroRatedSales view" must {
    behave like normalPage(createView(), messageKeyPrefix)
    behave like yesNoPage(
      form,
      createViewUsingForm,
      messageKeyPrefix,
      routes.ZeroRatedSalesController.onSubmit().url
    )

    val doc = asDocument(createViewUsingForm(form))

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

    "have a link" in {
      doc.select(Selectors.link(1)).text() mustBe linkText
    }
  }
}
