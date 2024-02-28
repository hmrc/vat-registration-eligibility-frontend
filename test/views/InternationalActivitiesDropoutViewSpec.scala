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

import views.html.InternationalActivityDropout

class InternationalActivitiesDropoutViewSpec extends ViewSpecBase {

  object Selectors extends BaseSelectors

  val view = app.injector.instanceOf[InternationalActivityDropout]
  val messageKeyPrefix = "eligibility.international"

  val h1 = "The business must register for VAT using a different service"
  val paragraph = "If the business:"
  val bullet1 = "is selling assets that you have claimed a repayment of VAT under Directive 2008/9 or 13th Directive refund arrangements - "
  val link1 = "register using form VAT1C"
  val bullet2 = "is selling goods into Northern Ireland from an EU member state - "
  val link2 = "register using form VAT1A"

  "InternationalActivitiesDropout view" when {

    lazy val doc = asDocument(view()(fakeCacheDataRequestIncorped, messages, frontendAppConfig))

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
        doc.select(Selectors.p(1)).text() mustBe paragraph
      }

      "display the bullet and link text correctly" in {
        doc.select(Selectors.bullet(1)).first().text() mustBe bullet1 + link1
        doc.select(Selectors.bullet(2)).first().text() mustBe bullet2 + link2
      }

  }
  

}
