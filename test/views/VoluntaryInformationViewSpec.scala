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

import forms.VoluntaryInformationFormProvider
import models.NormalMode
import views.html.voluntaryInformation

class VoluntaryInformationViewSpec extends ViewSpecBase {
  val form = new VoluntaryInformationFormProvider()()

  val view = app.injector.instanceOf[voluntaryInformation]

  object ExpectedContent {
    val heading = "Do you want to sign the business up to Making Tax Digital for VAT?"
    val text1 = "Making Tax Digital for VAT means using software to submit VAT Returns directly to HMRC. From April 2022 all VAT-registered businesses will be signed up for Making Tax Digital for VAT. "
    val text2 = "You do not need to sign the business up for Making Tax Digital for VAT yet as the business’s taxable turnover is below the threshold. However, it might find it convenient to start using compatible software to keep VAT records and submit them directly to HMRC before it’s signed up in April 2022. "
    val text3 = "Find out more about Making Tax Digital for VAT (opens in new tab)"
    val para = text1 + text2 + text3
  }

  object Selectors extends BaseSelectors

  "BusinessEntity view" must {
    lazy val doc = asDocument(view(form, NormalMode)(fakeDataRequest, messages, frontendAppConfig))

    "have a heading" in {
      doc.select(Selectors.h1).text() mustBe ExpectedContent.heading
    }

    "have a paragraph" in {
      doc.select(Selectors.p(1)).text() mustBe ExpectedContent.para
    }

    "have a set of radio inputs" which {

      "for the option 'Yes, sign the business up to Making Tax Digital'" should {

        "have the text 'Yes, sign the business up to Making Tax Digital'" in {
          doc.select("label[for=value]").text() mustEqual messages("voluntaryInformation.radioyes")
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#value")

          "have the id 'sole-trader'" in {
            optionLabel.attr("id") mustEqual "value"
          }

          "be of type radio" in {
            optionLabel.attr("type") mustEqual "radio"
          }
        }
      }

      "for the option 'No, do not sign the business up to Making Tax Digital'" should {

        "have the text 'No, do not sign the business up to Making Tax Digital'" in {
          doc.select("label[for=value-no]").text() mustEqual messages("voluntaryInformation.radiono")
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#value-no")

          "have the id 'sole-trader'" in {
            optionLabel.attr("id") mustEqual "value-no"
          }

          "be of type radio" in {
            optionLabel.attr("type") mustEqual "radio"
          }
        }
      }
    }
  }
}
