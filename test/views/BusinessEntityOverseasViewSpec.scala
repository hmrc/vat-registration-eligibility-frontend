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

import controllers.routes
import forms.BusinessEntityOverseasFormProvider
import views.html.BusinessEntityOverseas

class BusinessEntityOverseasViewSpec extends ViewSpecBase {

  val messageKeyPrefix = "BusinessEntityOverseas"
  val form = new BusinessEntityOverseasFormProvider()()
  val view: BusinessEntityOverseas = app.injector.instanceOf[BusinessEntityOverseas]

  object Selectors extends BaseSelectors

  val h1 = "What type of business do you want to register for VAT?"
  val radio1 = "Non-UK company"
  val radio2 = "Sole trader"
  val radio3 = "UK company"
  val radio4 = "General partnership"
  val radio5 = "Limited liability partnership"
  val radio6 = "Trust (unincorporated)"
  val hint = "A NETP is any person who is not normally resident in the UK or does not have a UK establishment."

  val detailsSummary = "Business type descriptions"
  val provider1 = "Non-UK company"
  val detailsPara1 = "This is any company incorporated outside the jurisdiction of the UK."
  val provider2 = "Sole trader"
  val detailsPara2 = "Sole traders work for themselves, are classed as self-employed and make all the business decisions."
  val provider3 = "UK company"
  val detailsPara3 = "This is a company that is registered with Companies House in the UK. It includes limited or unlimited companies."
  val provider4 = "General partnership"
  val detailsPara4: String = "These are made up of 2 or more people. One of the partners will be ‘nominated’ to deal with HMRC. " +
    "General partnerships have no legal existence separate to the partners themselves. Most partnerships are of this type."
  val provider5 = "Limited liability partnership"
  val detailsPara5: String = "These must be registered at Companies House. " +
    "They are taxed as partnerships, but they have the benefits of being a corporate entity. All the partners have limited liability for debts."
  val provider6 = "Trust (unincorporated)"
  val detailsPara6: String = "Trusts have no legal identity of their own. " +
    "They hold assets on behalf of an individual or another organisation and govern how they are to be used. They are run by a group of people called trustees."


  "BusinessEntityOverseas view" must {
    lazy val doc = asDocument(view(form, routes.BusinessEntityOverseasController.onSubmit())(fakeDataRequestIncorped, messages, frontendAppConfig))

    "have the correct continue button" in {
      doc.select(Selectors.button).text() mustBe continueButton
    }

    "have the correct back link" in {
      doc.select(Selectors.backLink).text() mustBe backLink
    }

    "have the correct browser title" in {
      doc.select(Selectors.title).text() mustBe title(h1)
    }

    "have the correct heading" in {
      doc.select(Selectors.h1).text() mustBe h1
    }

    "have the right radio options" in {
      doc.select(Selectors.radio(1)).text() mustBe radio1
      doc.select(Selectors.radio(2)).text() mustBe radio2
      doc.select(Selectors.radio(3)).text() mustBe radio3
      doc.select(Selectors.radio(4)).text() mustBe radio4
      doc.select(Selectors.radio(5)).text() mustBe radio5
      doc.select(Selectors.radio(6)).text() mustBe radio6
    }

    "have the right details summary" in {
      doc.select(Selectors.detailsSummary).text() mustBe detailsSummary
    }

    "have the right details content" in {
      doc.select(Selectors.detailsContent).text() must
        (
          include(provider1) and
            include(detailsPara1) and
            include(provider2) and
            include(detailsPara2) and
            include(provider3) and
            include(detailsPara3) and
            include(provider4) and
            include(detailsPara4) and
            include(provider5) and
            include(detailsPara5) and
            include(provider6) and
            include(detailsPara6)
          )
    }
  }
}
