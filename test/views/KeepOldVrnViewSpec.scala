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

import forms.KeepOldVrnFormProvider
import views.html.KeepOldVrn

class KeepOldVrnViewSpec extends ViewSpecBase {

  val messageKeyPrefix = "keepOldVrn"
  val togc = "togc"
  val cole = "cole"
  val own = "own"


  val h1 = "Do you want to keep the existing VAT registration number?"
  val para = "To keep the number:"
  val togcBullet1 = "both you and the previous owner must complete the Request for transfer of a VAT registration number form (opens in a new tab)."
  val togcBullet2 = "you must apply to register for VAT no more than 30 days after the request for a transfer of a VAT registration number form has been received by HMRC"
  val coleBullet1 = "the previous entity must complete the Request for transfer of a VAT registration number form (opens in a new tab)."
  val coleBullet2 = "you must apply to register for VAT no more than 30 days after the request for a transfer of a VAT registration number form has been received by HMRC"

  val view = app.injector.instanceOf[KeepOldVrn]

  object Selectors extends BaseSelectors

  "Keep VAT Number view" when {
    s"registration reason is $togc" must {
      val form = new KeepOldVrnFormProvider()(togc,own)
      lazy val doc = asDocument(view(form, togc,own)(fakeDataRequest, messages, frontendAppConfig))

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

      "have the correct paragraph" in {
        doc.select(Selectors.p(1)).text() mustBe para
      }

      "have the correct bullets" in {
        doc.select(Selectors.bullet(1)).text() mustBe togcBullet1
        doc.select(Selectors.bullet(2)).text() mustBe togcBullet2
      }
    }

    s"registration reason is $cole" must {
      val form = new KeepOldVrnFormProvider()(cole,own)
      lazy val doc = asDocument(view(form, cole,own)(fakeDataRequest, messages, frontendAppConfig))

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

      "have the correct paragraph" in {
        doc.select(Selectors.p(1)).text() mustBe para
      }

      "have the correct bullets" in {
        doc.select(Selectors.bullet(1)).text() mustBe coleBullet1
        doc.select(Selectors.bullet(2)).text() mustBe coleBullet2
      }
    }
  }
}
