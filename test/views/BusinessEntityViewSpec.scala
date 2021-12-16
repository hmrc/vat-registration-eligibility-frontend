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
import forms.BusinessEntityFormProvider
import org.jsoup.Jsoup
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.businessEntity

class BusinessEntityViewSpec extends ViewSpecBase {

  val formProvider = new BusinessEntityFormProvider()()
  implicit val appConfig: FrontendAppConfig = frontendAppConfig

  val testCall: Call = Call("POST", "/test-url")
  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  val view: businessEntity = app.injector.instanceOf[businessEntity]

  lazy val page: HtmlFormat.Appendable = view(
    formProvider,
    postAction = testCall
  )(
    request,
    messagesApi.preferred(request),
    appConfig
  )

  "BusinessEntity view" must {

    "have a set of radio inputs" which {
      lazy val doc = Jsoup.parse(page.body)

      "for the option 'Uk company'" should {

        "have the text 'Limited company'" in {
          doc.select("label[for=50]").text() mustEqual messages("businessEntity.limited-company")
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#50")

          "have the id '50'" in {
            optionLabel.attr("id") mustEqual "50"
          }

          "be of type radio" in {
            optionLabel.attr("type") mustEqual "radio"
          }
        }
      }

      "for the option 'Sole Trader'" should {

        "have the text 'Sole Trader'" in {
          doc.select("label[for=Z1]").text() mustEqual messages("businessEntity.soletrader")
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#Z1")

          "have the id 'Z1'" in {
            optionLabel.attr("id") mustEqual "Z1"
          }

          "be of type radio" in {
            optionLabel.attr("type") mustEqual "radio"
          }
        }
      }
      "for the option 'Partnership'" should {

        "have the text 'Partnership'" in {
          doc.select("label[for=partnership]").text() mustEqual messages("businessEntity.partnership")
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#partnership")

          "have the id 'partnership'" in {
            optionLabel.attr("id") mustEqual "partnership"
          }

          "be of type radio" in {
            optionLabel.attr("type") mustEqual "radio"
          }
        }
      }

      "for the option 'Other'" should {

        "have the text 'Other'" in {
          doc.select("label[for=other]").text() mustEqual messages("businessEntity.other")
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#other")

          "have the id 'other'" in {
            optionLabel.attr("id") mustEqual "other"
          }

          "be of type radio" in {
            optionLabel.attr("type") mustEqual "radio"
          }
        }
      }
    }
  }
}
