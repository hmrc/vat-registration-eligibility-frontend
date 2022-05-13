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

import featureswitch.core.config.{OBIFlow, TOGCFlow, VATGroupFlow}
import forms.InvolvedInOtherBusinessFormProvider
import models.NormalMode
import views.html.InvolvedInOtherBusiness

class InvolvedInOtherBusinessViewSpec extends ViewSpecBase {

  val messageKeyPrefix = "involvedInOtherBusiness"
  val form = new InvolvedInOtherBusinessFormProvider().form

  val h1 = "Have you been involved with another business or taken over a VAT-registered business?"
  val obiH1 = "Have you been involved with another business?"
  val togcH1 = "Have you taken over a VAT-registered business?"
  val vatGroupH1 = "Do you want to set up a VAT group?"
  val bullet1 = "over the past 2 years, you have had another self-employed business in the UK or Isle of Man (do not tell us if your only source of self-employed income was from being a landlord)"
  val bullet2 = "over the past 2 years, you have been a partner or director with a different business in the UK or Isle of Man"
  val vatGroupBullet = "you want to set up a VAT group (opens in new tab)."
  val bullet4 = "the company used to be a different type of VAT-registered business, for example a sole trader"
  val bullet5 = "the company has taken over another VAT-registered company that was making a profit"

  val view = app.injector.instanceOf[InvolvedInOtherBusiness]

  object Selectors extends BaseSelectors

  "InvolvedInOtherBusiness view" when {
    "showVatGroupBullet is set to true" must {
      lazy val doc = asDocument(view(form, NormalMode)(fakeRequest, messages, frontendAppConfig))

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

      "have the correct heading when the TOGC/COLE flow is enabled" in {
        enable(TOGCFlow)
        val doc = asDocument(view(form, NormalMode)(fakeRequest, messages, frontendAppConfig))

        doc.select(Selectors.h1).text() mustBe obiH1
        disable(TOGCFlow)
      }

      "have the correct heading when the OBI flow is enabled" in {
        enable(OBIFlow)
        val doc = asDocument(view(form, NormalMode)(fakeRequest, messages, frontendAppConfig))

        doc.select(Selectors.h1).text() mustBe togcH1
        disable(OBIFlow)
      }

      "have the correct heading when the OBI and TOGC flow are enabled" in {
        enable(OBIFlow)
        enable(TOGCFlow)
        val doc = asDocument(view(form, NormalMode)(fakeRequest, messages, frontendAppConfig))

        doc.select(Selectors.h1).text() mustBe vatGroupH1
        disable(OBIFlow)
        disable(TOGCFlow)
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

      "display the bullet text correctly if Vat Group flow is enabled" in {
        enable(VATGroupFlow)
        val doc = asDocument(view(form, NormalMode)(fakeRequest, messages, frontendAppConfig))

        doc.select(Selectors.bullet(1)).first().text() mustBe bullet1
        doc.select(Selectors.bullet(2)).first().text() mustBe bullet2
        doc.select(Selectors.bullet(3)).first().text() mustBe bullet4
        doc.select(Selectors.bullet(4)).first().text() mustBe bullet5
        doc.select(Selectors.bullet(5)).headOption mustBe None
        disable(VATGroupFlow)
      }

      "display the bullet text correctly if TOGC/COLE flow is enabled" in {
        enable(TOGCFlow)
        val doc = asDocument(view(form, NormalMode)(fakeRequest, messages, frontendAppConfig))

        doc.select(Selectors.bullet(1)).first().text() mustBe bullet1
        doc.select(Selectors.bullet(2)).first().text() mustBe bullet2
        doc.select(Selectors.bullet(3)).first().text() mustBe vatGroupBullet
        doc.select(Selectors.bullet(4)).headOption mustBe None
        disable(TOGCFlow)
      }

      "display the bullet text correctly when OBI flow is enabled" in {
        enable(OBIFlow)
        val doc = asDocument(view(form, NormalMode)(fakeRequest, messages, frontendAppConfig))

        doc.select(Selectors.bullet(1)).first().text() mustBe vatGroupBullet
        doc.select(Selectors.bullet(2)).first().text() mustBe bullet4
        doc.select(Selectors.bullet(3)).first().text() mustBe bullet5
        doc.select(Selectors.bullet(4)).headOption mustBe None
        disable(OBIFlow)
      }

      "display the bullet text correctly when OBI and TOGC flows are enabled" in {
        enable(OBIFlow)
        enable(TOGCFlow)
        val doc = asDocument(view(form, NormalMode)(fakeRequest, messages, frontendAppConfig))

        doc.select(Selectors.bullet(1)).first().text() mustBe vatGroupBullet
        doc.select(Selectors.bullet(2)).headOption mustBe None
        disable(OBIFlow)
        disable(TOGCFlow)
      }
    }
  }
}
