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

import featureswitch.core.config.FeatureSwitching
import helpers._
import identifiers.PreviousBusinessNameId
import org.jsoup.Jsoup
import play.api.http.Status._
import play.mvc.Http.HeaderNames

class PreviousBusinessNameControllerISpec extends IntegrationSpecBase with FeatureSwitching with S4LStub {

  val pageUrl = "/previous-business-name"
  val testPreviousBusinessName = "Al Pacino Ltd"
  val textbox = "previousBusinessName"

  s"GET /previous-business-name" when {
    "an answer exists for the page" must {
      "return OK with the answer pre-populated" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        cacheSessionData(sessionIdStr, PreviousBusinessNameId, testPreviousBusinessName)

        val res = await(buildClient(pageUrl).get)
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.textboxContainsValue(textbox, testPreviousBusinessName) mustBe true
      }
    }
    "an answer doesn't exist for the page" must {
      "return OK with an empty form" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        val nothing = ""

        val res = await(buildClient(pageUrl).get)
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.textboxContainsValue(textbox, nothing) mustBe true
      }
    }
  }

  s"POST /previous-business-name" must {
    "redirect to Previous VRN" in new Setup {
      stubSuccessfulLogin()
      stubAudits()
      stubS4LGetNothing(testRegId)

      val res = await(buildClient(pageUrl).post(Map(textbox -> testPreviousBusinessName)))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VATNumberController.onPageLoad.url)
    }
    "return a BAD_REQUEST with form errors" in new Setup {
      stubSuccessfulLogin()
      stubAudits()
      stubS4LGetNothing(testRegId)

      val res = await(buildClient(pageUrl).post(Map(textbox -> "")))

      res.status mustBe BAD_REQUEST
    }
  }
}