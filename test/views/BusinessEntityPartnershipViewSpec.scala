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

import config.FrontendAppConfig
import forms.BusinessEntityPartnershipFormProvider
import org.jsoup.Jsoup
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.businessEntityPartnership

class BusinessEntityPartnershipViewSpec extends ViewSpecBase {

  val formProvider = new BusinessEntityPartnershipFormProvider()()
  implicit val appConfig: FrontendAppConfig = frontendAppConfig

  object Selectors extends BaseSelectors

  val testCall: Call = Call("POST", "/test-url")
  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  val view: businessEntityPartnership = app.injector.instanceOf[businessEntityPartnership]

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
  val detailsPara1 = "Made up of 2 or more people, usually sole traders. One of the partners will be ‘nominated’ to deal with HMRC. Has no legal existence distinct from the partners themselves. Most partnerships are of this type."
  val provider2 = "Limited partnership"
  val detailsPara2 = "Made up of general and limited partners. Must register with Companies House but does not have to file an annual return. The limited partners will have limited liability for debts."
  val provider3 = "Scottish partnership"
  val detailsPara3 = "Same as a general partnership but is a separate legal entity distinct from the partners themselves. The business address must be registered in Scotland."
  val provider4 = "Scottish limited partnership"
  val detailsPara4 = "Similar to a limited partnership. Made up of general and limited partners. It has a separate legal entity and the limited partners have limited liability for debts. The business address must be registered in Scotland. "
  val provider5 = "Limited liability partnership"
  val detailsPara5 = "Must be registered at Companies House. Taxed as partnerships but have the benefits of being a corporate entity. All the partners have limited liability for debts."

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
      doc.getElementById(Selectors.backLink).text() mustBe backLink
    }
  }
}
