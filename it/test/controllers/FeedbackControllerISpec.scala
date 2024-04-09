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

package controllers

import helpers.IntegrationSpecBase
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class FeedbackControllerISpec extends IntegrationSpecBase {

  val pageUrl = "/feedback"

  "GET /feedback" must {
    "redirect to the feedback form" in new Setup {
      stubSuccessfulLogin()
      stubAudits()

      val res = await(buildClient(pageUrl).get)
      val referrer = res.headers.get(REFERER).getOrElse("")

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(s"${appConfig.betaFeedbackUrl}&backUrl=$referrer")
    }
  }

}