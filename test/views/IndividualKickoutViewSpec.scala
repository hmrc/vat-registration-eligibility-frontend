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

import views.html.IndividualKickout

class IndividualKickoutViewSpec extends ViewSpecBase {

  val messageKeyPrefix = "applyInWriting"

  object Selectors extends BaseSelectors

  val h1 = "Sorry, the service is unavailable"
  val p1 = "You cannot use this service to register for VAT."

  val view = app.injector.instanceOf[IndividualKickout]

  "ApplyInWriting view" must {
    lazy val doc = asDocument(view()(fakeRequest, messages, frontendAppConfig))

    "have the correct back link" in {
      doc.select(Selectors.backLink).text() mustBe backLink
    }

    "have the correct browser title" in {
      doc.select(Selectors.title).text() mustBe title(h1)
    }

    "have the correct heading" in {
      doc.select(Selectors.h1).text() mustBe h1
    }

    "have the first paragraph" in {
      doc.select(Selectors.p(1)).text() mustBe p1
    }
  }
}
