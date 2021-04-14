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

import forms.InternationalActivitiesFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.html.internationalActivities

class InternationalActivitiesViewSpec extends ViewSpecBase {

  val messageKeyPrefix = "internationalActivities"
  val form = new InternationalActivitiesFormProvider()()

  val h1 = "Will the business do any of the following activities over the next 12 months?"
  val paragraph = "Tell us if the business will:"
  val bullet1 = "do all its business outside the UK"
  val bullet2 = "have its head office outside the UK"
  val bullet3 = "sell assets bought from outside the UK and claim a repayment of VAT under Directive 2008/9EC or Thirteenth VAT Directive"
  val bullet4 = "sell goods located in Northern Ireland at the time of sale"
  val bullet5 = "sell or move goods from Northern Ireland to an EU member state"
  val bullet6 = "bring goods into Northern Ireland from an EU member state"
  val bullet7 = "sell goods into Northern Ireland from an EU member state"

  val view = app.injector.instanceOf[internationalActivities]

  object Selectors extends BaseSelectors

  "InternationalActivities view" must {
    lazy val doc = asDocument(view(form, NormalMode)(fakeDataRequest, messages, frontendAppConfig))

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

    "have the correct paragraph" in {
      doc.select(Selectors.p(1)).text() mustBe paragraph
    }

    "display the bullet text correctly" in {
      doc.select(Selectors.bullet(1)).first().text() mustBe bullet1
      doc.select(Selectors.bullet(2)).first().text() mustBe bullet2
      doc.select(Selectors.bullet(3)).first().text() mustBe bullet3
      doc.select(Selectors.bullet(4)).first().text() mustBe bullet4
      doc.select(Selectors.bullet(5)).first().text() mustBe bullet5
    }

  }

}
