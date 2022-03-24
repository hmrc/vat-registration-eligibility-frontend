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
  val para = "To keep the number:"
  val togcBullet1 = "the previous owner must deregister the business for VAT, ideally using the ‘Change registration details’ online service"
  val togcBullet2 = "you must apply for VAT no more than 30 days after the previous owner applies to deregister"
  val coleBullet1 = "the previous entity must deregister for VAT, ideally using the ‘Change registration details’ online service"
  val coleBullet2 = "you must apply for VAT no more than 30 days after the previous entity applies to deregister"

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

      "have the correct paragraph" in {
        doc.select(Selectors.p(1)).text() mustBe para
      }

      "have the correct bullets" in {
        doc.select(Selectors.bullet(1)).text() mustBe togcBullet1
        doc.select(Selectors.bullet(2)).text() mustBe togcBullet2
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
