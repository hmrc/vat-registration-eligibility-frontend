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
import identifiers.RacehorsesId
import org.jsoup.Jsoup
import play.api.http.Status._
import play.api.libs.json.Format._
import play.api.libs.json.Json
import play.mvc.Http.HeaderNames

class RacehorsesControllerISpec extends IntegrationSpecBase with FeatureSwitching {

  val pageUrl = "/own-racehorses-buy-sell-property"
  val yesRadio = "value"
  val noRadio = "value-no"

  "GET /own-racehorses-buy-sell-property" when {
    "an answer exists for the page" must {
      "return OK with the answer pre-populated" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        cacheSessionData(sessionId, RacehorsesId, true)

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

  s"POST /own-racehorses-buy-sell-property" when {
    "the user answers" must {
      "navigate to Registering Business when false" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map("value" -> Seq("false"))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.RegisteringBusinessController.onPageLoad.url)
        verifySessionCacheData(sessionId, RacehorsesId, Option.apply[Boolean](false))
      }
      "navigate to VAT Exception when true" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map("value" -> Seq("true"))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VATExceptionKickoutController.onPageLoad.url)
        verifySessionCacheData(sessionId, RacehorsesId, Option.apply[Boolean](true))
      }
    }
    "the user doesn't answer" must {
      "return BAD_REQUEST" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Json.obj()))

        res.status mustBe BAD_REQUEST
      }
    }
  }
}