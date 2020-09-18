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

import views.html.eligible
import views.newbehaviours.ViewBehaviours

class EligibleViewSpec extends ViewBehaviours with BaseSelectors {

  val messageKeyPrefix = "eligible"
  implicit val msgs = messages

  val h1Text = "You can register for VAT online"
  val p1Text = "Based on your answers, you can register for VAT using the online service."
  val buttonText = "Continue to VAT registration"

  def createView = () => eligible()(fakeRequest, messages, frontendAppConfig)
  lazy val doc = asDocument(createView())

  "Introduction view" must {
    behave like normalPage(createView(), messageKeyPrefix)
    behave like pageWithHeading(createView(), h1Text)
    behave like pageWithSubmitButton(createView(), buttonText)

    "have the correct paragraph" in {
      doc.select(p(1)).first.text mustBe p1Text
    }
  }
}