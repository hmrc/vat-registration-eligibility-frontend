/*
 * Copyright 2020 HM Revenue & Customs
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
import helpers.IntegrationSpecBase
import identifiers.{BusinessEntityId, RegisteringBusinessId}
import models._
import org.jsoup.Jsoup
import play.api.http.Status.SEE_OTHER
import play.api.libs.json.Format._
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class RegisteringBusinessControllerISpec extends IntegrationSpecBase with FeatureSwitching {

  val pageUrl = "/whos-the-application-for"
  val internalId = "testInternalId"
  val ownRadio = "own"
  val someoneElsesRadio = "someone-else"

  "GET /whos-the-application-for" when {
    "an answer exists for the page" must {
      "return OK with the answer pre-populated" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        cacheSessionData[RegisteringBusiness](sessionIdStr, RegisteringBusinessId, OwnBusiness)

        val res = await(buildClient(pageUrl).get)
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.radioIsSelected(ownRadio) mustBe true
        doc.radioIsSelected(someoneElsesRadio) mustBe false
      }
    }
    "an answer doesn't exist for the page" must {
      "return OK with an empty form" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).get)
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.radioIsSelected(ownRadio) mustBe false
        doc.radioIsSelected(someoneElsesRadio) mustBe false
      }
    }
  }

  s"POST /whos-the-application-for" when {
    "the user answers" must {
      "navigate to Registration Reason Page when own business" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(controllers.routes.RegisteringBusinessController.onSubmit.url)
          .post(Map("value" -> Seq("own"))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.RegistrationReasonController.onPageLoad.url)
        verifySessionCacheData(sessionIdStr, RegisteringBusinessId, Option.apply[RegisteringBusiness](OwnBusiness))
      }

      "navigate to Registration Reason Page when someone else's business if Third Party Transactor flow" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(controllers.routes.RegisteringBusinessController.onSubmit.url)
          .post(Map("value" -> Seq("someone-else"))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.RegistrationReasonController.onPageLoad.url)
        verifySessionCacheData(sessionIdStr, RegisteringBusinessId, Option.apply[RegisteringBusiness](SomeoneElse))
      }

      "navigate to Registration Reason for own business when flow is NETP" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        cacheSessionData[BusinessEntity](sessionIdStr, BusinessEntityId, NETP)

        val res = await(buildClient(controllers.routes.RegisteringBusinessController.onSubmit.url)
          .post(Map("value" -> Seq("own"))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.RegistrationReasonController.onPageLoad.url)
        verifySessionCacheData(sessionIdStr, RegisteringBusinessId, Option.apply[RegisteringBusiness](OwnBusiness))
      }

      "navigate to Registration Reason for someone else's business when flow is Overseas and ThirdParty flows are enabled" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        cacheSessionData[BusinessEntity](sessionIdStr, BusinessEntityId, Overseas)

        val res = await(buildClient(controllers.routes.RegisteringBusinessController.onSubmit.url)
          .post(Map("value" -> Seq("someone-else"))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.RegistrationReasonController.onPageLoad.url)
        verifySessionCacheData(sessionIdStr, RegisteringBusinessId, Option.apply[RegisteringBusiness](SomeoneElse))
      }
    }
    "the user doesn't answer" must {
      "return BAD_REQUEST" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map[String, String]()))

        res.status mustBe BAD_REQUEST
      }
    }
  }
}