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

import config.FrontendAppConfig
import forms.BusinessEntityOtherFormProvider
import org.jsoup.Jsoup
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.BusinessEntityOther

class BusinessEntityOtherViewSpec extends ViewSpecBase {

  val formProvider = new BusinessEntityOtherFormProvider()()
  implicit val appConfig: FrontendAppConfig = frontendAppConfig

  object Selectors extends BaseSelectors

  val testCall: Call = Call("POST", "/test-url")
  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  val view: BusinessEntityOther = app.injector.instanceOf[BusinessEntityOther]

  lazy val page: HtmlFormat.Appendable = view(
    formProvider,
    postAction = testCall
  )(
    request,
    messagesApi.preferred(request),
    appConfig
  )

  val detailsSummary = "Business type descriptions"
  val provider1 = "Charitable Incorporated Organisation (CIO)"
  val detailsPara1 = "A legal entity for a registered charity. CIOs provide the benefits of incorporation but the organisation only needs to be registered with the Charity Commission and not Companies House."
  val provider2 = "Trust (non-incorporated)"
  val detailsPara2 = "Unincorporated with no legal identity of its own. A trust holds assets on behalf of an individual or another organisation and governs how they are to be used. Run by a group of people called trustees."
  val provider3 = "Registered Society"
  val detailsPara3 = "There are different kinds of registered societies, for example, Industrial and Provident Society, Co-operative Society (Co-op) and Community Benefit Society (BenCom). They have limited liability in the same way as companies."
  val provider4 = "Unincorporated Association"
  val detailsPara4 = "Groups that agree, or ‘contract’, to come together for a specific purpose. They normally have a constitution setting out the purpose for which the association has been set up and the rules for the association and its members."
  val provider5 = "Division"
  val detailsPara5 = "A business entity which is part of a larger group. It has 2 or more branches performing different functions or trading in different geographical regions."

  "BusinessEntityOther view" must {
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
