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

import views.html.ChoseNotToRegister

class ChoseNotToRegisterViewSpec extends ViewSpecBase {

  val messageKeyPrefix = "choseNotToRegister"

  val h1 = "You have chosen not to register the business for VAT"
  val finishButton = "Finish"
  val view = app.injector.instanceOf[ChoseNotToRegister]

  object Selectors extends BaseSelectors

  "ChoseNotToRegister view" must {
    lazy val doc = asDocument(view()(fakeCacheDataRequestIncorped, messages, frontendAppConfig))

    "have the correct continue button" in {
      doc.select(Selectors.button).text() mustBe finishButton
    }

    "have the correct back link" in {
      doc.select(Selectors.backLink).text() mustBe backLink
    }

    "have the correct browser title" in {
      doc.select(Selectors.title).first().text() mustBe title(h1)
    }

    "have the correct heading" in {
      doc.select(Selectors.h1).text() mustBe h1
    }
  }
}
