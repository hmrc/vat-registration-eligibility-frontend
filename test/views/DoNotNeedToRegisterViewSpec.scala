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

import views.html.DoNotNeedToRegister

class DoNotNeedToRegisterViewSpec extends ViewSpecBase {

  val messageKeyPrefix = "doNotNeedToRegister"
  val h1 = "Currently you do not need to register for VAT"
  val p1 = "From the answers provided, you do not need to register for VAT as you have not and do not plan to make taxable supplies in the UK."
  val p2 = "If this changes, the business should register for VAT."
  val button = "Finish and sign out"
  val view = app.injector.instanceOf[DoNotNeedToRegister]

  object Selectors extends BaseSelectors

  "DoNotNeedToRegister view" must {
    lazy val doc = asDocument(view()(fakeCacheDataRequestIncorped, messages, frontendAppConfig))

    "have a back link" in {
      doc.select(Selectors.backLink).text() mustBe backLink
    }

    "have the correct title" in {
      doc.select(Selectors.title).text() mustBe title(h1)
    }

    "have the correct heading" in {
      doc.select(Selectors.h1).text() mustBe h1
    }

    "have the correct text" in {
      doc.select(Selectors.p(1)).text() mustBe p1
      doc.select(Selectors.p(2)).text() mustBe p2
    }

    "have the correct button" in {
      doc.select(Selectors.button).text() mustBe button
    }
  }
}
