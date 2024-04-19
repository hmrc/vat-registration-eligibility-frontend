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

import helpers.{IntegrationSpecBase, S4LStub}
import play.api.http.Status._
import play.mvc.Http.HeaderNames

class TermsAndConditionsControllerISpec extends IntegrationSpecBase with S4LStub {

  val pageUrl = "/terms-and-conditions"

  s"GET /terms-and-conditions" must {
    "return OK" in new Setup {
      stubSuccessfulLogin()
      stubAudits()
      stubS4LGetNothing(testRegId)

      val res = await(buildClient(pageUrl).get)

      res.status mustBe OK
    }
  }

  s"POST /terms-and-conditions" when {
    "the answer is 'Yes'" must {
      "redirect to Mtd information page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        val res = await(buildClient(pageUrl).post(Map("termsAndConditions" -> Seq("true"))))

        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.MtdInformationController.onPageLoad.url)
      }
    }
    "the answer is 'No'" must {
      "redirect to Mtd information page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        val res = await(buildClient(pageUrl).post(Map("termsAndConditions" -> Seq("false"))))

        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.MtdInformationController.onPageLoad.url)
      }
    }
  }
}