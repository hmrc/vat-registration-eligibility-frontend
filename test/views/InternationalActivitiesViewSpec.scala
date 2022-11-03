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

import forms.InternationalActivitiesFormProvider
import models.NormalMode
import views.html.InternationalActivities

class InternationalActivitiesViewSpec extends ViewSpecBase {

  val messageKeyPrefix = "internationalActivities"
  val form = new InternationalActivitiesFormProvider()()

  val h1Business = "Will the business do any of the following activities over the next 12 months?"
  val h1Partnership = "Will the partnership do any of the following activities over the next 12 months?"
  val linkParagraph ="Use the GOV.UK Brexit checker (opens in new tab) to find out if the EU exit will impact your business."
  val paragraph = "Tell us if the business will:"
  val bullet1 = "sell assets bought from outside the UK and claim a repayment of VAT under Directive 2008/9EC or Thirteenth VAT Directive"
  val bullet2 = "sell goods into Northern Ireland from an EU member state"

  val view = app.injector.instanceOf[InternationalActivities]
  object Selectors extends BaseSelectors

  "InternationalActivities view" when {
    "Business entity is not partnership" must {

      val doc = asDocument(view(form, NormalMode)(fakeDataRequest, messages, frontendAppConfig))

      "have the correct continue button" in {
        doc.select(Selectors.button).text() mustBe continueButton
      }

      "have the correct back link" in {
        doc.select(Selectors.backLink).text() mustBe backLink
      }

      "have the correct browser title" in {
        doc.select(Selectors.title).text() mustBe title(h1Business)
      }

      "have the correct heading" in {
        doc.select(Selectors.h1).text() mustBe h1Business
      }

      "have the correct legend" in {
        doc.select(Selectors.legend(1)).text() mustBe h1Business
      }

      "have the correct link paragraph text" in {
        doc.select(Selectors.p(1)).text() mustBe linkParagraph
      }

      "have the correct paragraph" in {
        doc.select(Selectors.p(2)).text() mustBe paragraph
      }

      "display the bullet text correctly" in {
        doc.select(Selectors.bullet(1)).first().text() mustBe bullet1
        doc.select(Selectors.bullet(2)).first().text() mustBe bullet2
      }
    }
    "Business entity is partnership" must {

      val doc = asDocument(view(form, NormalMode, isPartnership = true)(fakeDataRequest, messages, frontendAppConfig))

      "have the correct continue button" in {
        doc.select(Selectors.button).text() mustBe continueButton
      }

      "have the correct back link" in {
        doc.select(Selectors.backLink).text() mustBe backLink
      }

      "have the correct browser title" in {
        doc.select(Selectors.title).text() mustBe title(h1Partnership)
      }

      "have the correct heading" in {
        doc.select(Selectors.h1).text() mustBe h1Partnership
      }

      "have the correct legend" in {
        doc.select(Selectors.legend(1)).text() mustBe h1Partnership
      }

      "have the correct link paragraph text" in {
        doc.select(Selectors.p(1)).text() mustBe linkParagraph
      }

      "have the correct paragraph" in {
        doc.select(Selectors.p(2)).text() mustBe paragraph
      }

      "display the bullet text correctly" in {
        doc.select(Selectors.bullet(1)).first().text() mustBe bullet1
        doc.select(Selectors.bullet(2)).first().text() mustBe bullet2
      }
    }
  }
  

}
