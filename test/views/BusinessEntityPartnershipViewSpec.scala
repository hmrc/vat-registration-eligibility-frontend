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

import config.FrontendAppConfig
import forms.BusinessEntityPartnershipFormProvider
import org.jsoup.Jsoup
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import views.html.BusinessEntityPartnership

class BusinessEntityPartnershipViewSpec extends ViewSpecBase {

  val formProvider = new BusinessEntityPartnershipFormProvider()()
  implicit val appConfig: FrontendAppConfig = frontendAppConfig

  object Selectors extends BaseSelectors

  val testCall: Call = Call("POST", "/test-url")
  val view: BusinessEntityPartnership = app.injector.instanceOf[BusinessEntityPartnership]

  lazy val page: HtmlFormat.Appendable = view(
    formProvider,
    postAction = testCall
  )(
    request,
    messagesApi.preferred(request),
    appConfig
  )

  val detailsSummary = "Partnership type descriptions"
  val provider1 = "General partnership"
  val detailsPara1 = "These are made up of 2 or more people. One of the partners will be ‘nominated’ to deal with HMRC. General partnerships have no legal existence separate to the partners themselves."
  val provider2 = "Limited partnership"
  val detailsPara2 = "These are made up of general and limited partners. They must register with Companies House but they do not have to file an annual return. The limited partners will have limited liability for debts."
  val provider3 = "Scottish partnership"
  val detailsPara3 = "These are the same as general partnerships but they are a legal entity, separate to the partners themselves. The business address must be registered in Scotland."
  val provider4 = "Scottish limited partnership"
  val detailsPara4 = "These are similar to limited partnerships. They are made up of general and limited partners. It has a separate legal entity and the limited partners have limited liability for debts. The business address must be registered in Scotland."
  val provider5 = "Limited liability partnership"
  val detailsPara5 = "These must be registered at Companies House. They are taxed as partnerships but they have the benefits of being a corporate entity. All the partners have limited liability for debts."

  "BusinessEntityPartnership view" must {
    lazy val doc = Jsoup.parse(page.body)

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
            include(detailsPara5)
          )
    }

    "have the right radio options" in {
      doc.select(Selectors.radio(1)).text() mustBe provider1
      doc.select(Selectors.radio(2)).text() mustBe provider2
      doc.select(Selectors.radio(3)).text() mustBe provider3
      doc.select(Selectors.radio(4)).text() mustBe provider4
      doc.select(Selectors.radio(5)).text() mustBe provider5
    }

    "have the correct continue button" in {
      doc.select(Selectors.button).text() mustBe continueButton
    }

    "have the correct back link" in {
      doc.select(Selectors.backLink).text() mustBe backLink
    }
  }
}
