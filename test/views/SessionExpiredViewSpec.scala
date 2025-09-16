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

import views.html.SessionExpired

class SessionExpiredViewSpec extends ViewSpecBase {

  object Selectors extends BaseSelectors

  val h1 = "For your security, this service has been reset"
  val p1 = "The details you have given have been deleted because you did not continue the service for 1 hour."
  val view = app.injector.instanceOf[SessionExpired]

  "Session Expired view" must {
    lazy val doc = asDocument(view()(fakeRequest, messages, frontendAppConfig))

    "have the correct browser title" in {
      doc.select(Selectors.title).first().text() mustBe title(h1)
    }

    "have the correct heading" in {
      doc.select(Selectors.h1).text() mustBe h1
    }

    "have the first paragraph" in {
      doc.select(Selectors.p(1)).text() mustBe p1
    }
  }
}
