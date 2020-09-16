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
import forms.InvolvedInOtherBusinessFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.newbehaviours.YesNoViewBehaviours
import views.html.involvedInOtherBusiness

class InvolvedInOtherBusinessViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "involvedInOtherBusiness"
  val form = new InvolvedInOtherBusinessFormProvider().form
  implicit val msgs = messages

  def createView(): HtmlFormat.Appendable = involvedInOtherBusiness(form, NormalMode)(fakeRequest, messages, frontendAppConfig)

  def createViewUsingForm(): Form[_] => HtmlFormat.Appendable =
    (form: Form[_]) => involvedInOtherBusiness(form, NormalMode)(fakeRequest, messages, frontendAppConfig)

  "InvolvedInOtherBusiness view with no acting on behalf of officer" must {
    behave like normalPage(createView(), messageKeyPrefix)
    behave like yesNoPage(form, createViewUsingForm(), messageKeyPrefix, routes.InvolvedInOtherBusinessController.onSubmit().url)

    "display the right headings and bullets correctly" in {
      val doc = asDocument(createView())
      doc.select("h1").text() mustBe "Have you been involved with another business or taken over a VAT-registered business?"
      doc.select("li:nth-of-type(1)").first().text() mustBe "over the past 2 years, you have had another self-employed business in the UK or Isle of Man (do not tell us if your only source of self-employed income was from being a landlord)"
      doc.select("li:nth-of-type(5)").first().text() mustBe "the company has taken over another VAT-registered company that was making a profit"
    }
  }

}
