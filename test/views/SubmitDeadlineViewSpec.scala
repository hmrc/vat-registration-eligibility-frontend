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

import views.html.SubmitDeadlineView

class SubmitDeadlineViewSpec extends ViewSpecBase {

  object ExpectedContent {
    val h1 = "You must submit this VAT registration application by 19 May 2025, or you will need to start again"
    val p1 = "The service is being updated on 19 May 2025."
    val p2 = "If you don’t complete and submit this application before this date, you’ll have to start the application again."
    val p3 = "After 19 May 2025, you’ll have 28 days to complete a VAT application each time you save your progress."
    val buttonText = "Continue"
  }

  val view: SubmitDeadlineView = app.injector.instanceOf[SubmitDeadlineView]

  object Selectors extends BaseSelectors

  "Introduction view" must {
    lazy val doc = asDocument(view()(fakeRequest, messages, frontendAppConfig))

    "have the correct continue button" in {
      doc.select(Selectors.button).text() mustBe ExpectedContent.buttonText
    }

    "have the correct browser title" in {
      doc.select(Selectors.title).first().text() mustBe title(ExpectedContent.h1)
    }

    "have the correct heading" in {
      doc.select(Selectors.h1).text() mustBe ExpectedContent.h1
    }

    "have the correct paragraphs" in {
      doc.select(Selectors.p(1)).first.text mustBe ExpectedContent.p1
      doc.select(Selectors.p(2)).first.text mustBe ExpectedContent.p2
      doc.select(Selectors.p(3)).first.text mustBe ExpectedContent.p3
    }
  }
}
