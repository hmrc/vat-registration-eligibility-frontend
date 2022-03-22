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

import forms.KeepOldVrnFormProvider
import views.html.KeepOldVrn

class KeepOldVrnViewSpec extends ViewSpecBase {

  val messageKeyPrefix = "keepOldVrn"
  val togc = "togc"
  val cole = "cole"

  val h1 = "Do you want to keep the existing VAT registration number?"
  val togcText1 = "The date of transfer you entered must match the date provided to HM Revenue and Customs (HMRC) by the previous owner of the business."
  val togcText2 = "The Transfer of a Going Concern (TOGC) will only be completed when the previous owner has successfully deregistered the business for VAT. It is recommended that they deregister and notify HMRC of the transfer using the ‘Change registration details’ online service."
  val togcText3 = "For the purpose of VAT Registration you are required to obtain and keep the trading records for the business."
  val coleText1 = "The date of transfer you entered must match the date provided to HM Revenue and Customs (HMRC) by the previous legal entity."
  val coleText2 = "The quickest way for them to do this would be to use the ‘Change registration details’ service, and select the option ‘Change of Legal Entity’ which can be accessed via HMRC Online Services. If they use this service, we can deregister their business without cancelling their VAT registration number."
  val coleText3 = "You will need to apply to register for VAT within 30 days of the old entity applying to cancel their registration. If you don’t meet this deadline, their VAT registration number will be cancelled automatically, and we won’t be able to transfer it to you. Instead, we will issue you with a new VAT registration number."

  val view = app.injector.instanceOf[KeepOldVrn]

  object Selectors extends BaseSelectors

  "Keep VAT Number view" when {
    s"registration reason is $togc" must {
      val form = new KeepOldVrnFormProvider()(togc)
      lazy val doc = asDocument(view(form, togc)(fakeDataRequest, messages, frontendAppConfig))

      "have the correct continue button" in {
        doc.select(Selectors.button).text() mustBe continueButton
      }

      "have the correct back link" in {
        doc.getElementById(Selectors.backLink).text() mustBe backLink
      }

      "have the correct browser title" in {
        doc.select(Selectors.title).text() mustBe title(h1)
      }

      "have the correct heading" in {
        doc.select(Selectors.h1).text() mustBe h1
      }

      "have the correct yes text" in {
        doc.select(Selectors.p(1)).text() mustBe togcText1
        doc.select(Selectors.p(2)).text() mustBe togcText2
        doc.select(Selectors.p(3)).text() mustBe togcText3
      }
    }

    s"registration reason is $cole" must {
      val form = new KeepOldVrnFormProvider()(cole)
      lazy val doc = asDocument(view(form, cole)(fakeDataRequest, messages, frontendAppConfig))

      "have the correct continue button" in {
        doc.select(Selectors.button).text() mustBe continueButton
      }

      "have the correct back link" in {
        doc.getElementById(Selectors.backLink).text() mustBe backLink
      }

      "have the correct browser title" in {
        doc.select(Selectors.title).text() mustBe title(h1)
      }

      "have the correct heading" in {
        doc.select(Selectors.h1).text() mustBe h1
      }

      "have the correct yes text" in {
        doc.select(Selectors.p(1)).text() mustBe coleText1
        doc.select(Selectors.p(2)).text() mustBe coleText2
        doc.select(Selectors.p(3)).text() mustBe coleText3
      }
    }
  }
}
