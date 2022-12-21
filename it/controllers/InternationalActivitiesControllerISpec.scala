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

import featureswitch.core.config._
import helpers.{IntegrationSpecBase, S4LStub}
import identifiers.{BusinessEntityId, InternationalActivitiesId}
import models._
import org.jsoup.Jsoup
import play.api.http.Status._
import play.api.libs.json.Format._
import play.mvc.Http.HeaderNames

class InternationalActivitiesControllerISpec extends IntegrationSpecBase with FeatureSwitching with S4LStub {

  val pageUrl = "/business-activities-next-12-months"
  val yesRadio = "value"
  val noRadio = "value-no"

  "GET /business-activities-next-12-months" when {
    "an answer exists for the page" must {
      "return OK with the answer pre-populated" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        cacheSessionData(sessionId, InternationalActivitiesId, true)

        val res = await(buildClient(pageUrl).get)
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.radioIsSelected(yesRadio) mustBe true
        doc.radioIsSelected(noRadio) mustBe false
      }
    }
    "an answer doesn't exist for the page" must {
      "return OK with an empty form" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).get)
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.radioIsSelected(yesRadio) mustBe false
        doc.radioIsSelected(noRadio) mustBe false
      }
    }
  }

  s"POST /business-activities-next-12-months" when {
    "the user answers" must {
      "navigate to International Activities dropout when true" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        cacheSessionData[BusinessEntity](sessionId, BusinessEntityId, UKCompany)

        val res = await(buildClient(controllers.routes.InternationalActivitiesController.onSubmit().url)
          .post(Map("value" -> Seq("true"))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.EligibilityDropoutController.internationalActivitiesDropout().url)
        verifySessionCacheData(sessionId, InternationalActivitiesId, Option.apply[Boolean](true))
      }

      "navigate to Registering Business when false" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        cacheSessionData[BusinessEntity](sessionId, BusinessEntityId, UKCompany)

        val res = await(buildClient(controllers.routes.InternationalActivitiesController.onSubmit().url)
          .post(Map("value" -> Seq("false"))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.RegisteringBusinessController.onPageLoad.url)
        verifySessionCacheData(sessionId, InternationalActivitiesId, Option.apply[Boolean](false))
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