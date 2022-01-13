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

import forms.InvolvedInOtherBusinessFormProvider
import models.NormalMode
import views.html.involvedInOtherBusiness

class InvolvedInOtherBusinessViewSpec extends ViewSpecBase {

  val messageKeyPrefix = "involvedInOtherBusiness"
  val form = new InvolvedInOtherBusinessFormProvider().form

  val h1 = "Have you been involved with another business or taken over a VAT-registered business?"

  val bullet1 = "over the past 2 years, you have had another self-employed business in the UK or Isle of Man (do not tell us if your only source of self-employed income was from being a landlord)"
  val bullet2 = "over the past 2 years, you have been a partner or director with a different business in the UK or Isle of Man"
  val vatGroupBullet = "you want to set up a VAT group (opens in new tab)."
  val bullet4 = "the company used to be a different type of VAT-registered business, for example a sole trader"
  val bullet5 = "the company has taken over another VAT-registered company that was making a profit"

  val view = app.injector.instanceOf[involvedInOtherBusiness]

  object Selectors extends BaseSelectors

  "InvolvedInOtherBusiness view" when {
    "showVatGroupBullet is set to true" must {
      lazy val doc = asDocument(view(form, NormalMode, true)(fakeRequest, messages, frontendAppConfig))

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

      "have the correct legend" in {
        doc.select(Selectors.legend(1)).text() mustBe h1
      }

      "display the bullet text correctly" in {
        doc.select(Selectors.bullet(1)).first().text() mustBe bullet1
        doc.select(Selectors.bullet(2)).first().text() mustBe bullet2
        doc.select(Selectors.bullet(3)).first().text() mustBe vatGroupBullet
        doc.select(Selectors.bullet(4)).first().text() mustBe bullet4
        doc.select(Selectors.bullet(5)).first().text() mustBe bullet5
      }
    }
    "showVatGroupBullet is set to false" must {
      lazy val doc = asDocument(view(form, NormalMode, false)(fakeRequest, messages, frontendAppConfig))

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

      "have the correct legend" in {
        doc.select(Selectors.legend(1)).text() mustBe h1
      }

      "display the bullet text correctly" in {
        doc.select(Selectors.bullet(1)).first().text() mustBe bullet1
        doc.select(Selectors.bullet(2)).first().text() mustBe bullet2
        doc.select(Selectors.bullet(3)).first().text() mustBe bullet4
        doc.select(Selectors.bullet(4)).first().text() mustBe bullet5
      }
    }

  }


}
