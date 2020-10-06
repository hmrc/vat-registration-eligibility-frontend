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

import forms.VoluntaryInformationFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.voluntaryInformation
import views.newbehaviours.ViewBehaviours

class VoluntaryInformationViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "voluntaryInformation"
  val form = new VoluntaryInformationFormProvider()()

  object Selectors extends BaseSelectors

  def createView: () => HtmlFormat.Appendable =
    () => voluntaryInformation(form, NormalMode)(fakeDataRequest, messages, frontendAppConfig)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable =
    (form: Form[_]) => voluntaryInformation(form, NormalMode)(fakeDataRequest, messages, frontendAppConfig)

  "BusinessEntity view" must {

    "have a set of radio inputs" which {
      val doc = asDocument(createViewUsingForm(form))

      "for the option 'Yes, I would like to sign up to Making Tax Digital'" should {

        "have the text 'Yes, I would like to sign up to Making Tax Digital'" in {
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

      "for the option 'No, I do not want to sign up to Making Tax Digital'" should {

        "have the text 'No, I do not want to sign up to Making Tax Digital'" in {
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
