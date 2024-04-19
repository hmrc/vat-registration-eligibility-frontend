/*
 * Copyright 2017 HM Revenue & Customs
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
import identifiers.BusinessEntityId
import models.BusinessEntity.{netpKey, overseasKey}
import models.{BusinessEntity, NETP, Overseas}
import org.jsoup.Jsoup
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class BusinessEntityOverseasControllerISpec extends IntegrationSpecBase {

  val pageUrl = "/business-entity-overseas"

  "GET /business-entity-overseas" when {
    "an answer aleady exists for the page" must {
      "return OK with the answer pre-populated" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        cacheSessionData[BusinessEntity](sessionIdStr, BusinessEntityId, NETP)

        val res = await(buildClient(pageUrl).get)
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.radioIsSelected(netpKey) mustBe true
        doc.radioIsSelected(overseasKey) mustBe false
      }
    }
    "an answer doesn't exist for the page" must {
      "return OK with an empty form" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).get)
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.radioIsSelected(netpKey) mustBe false
        doc.radioIsSelected(overseasKey) mustBe false
      }
    }
  }

  s"POST /business-entity-overseas" when {
    "the user answers" must {
      "return a redirect to Agricultural Flat Rate Scheme when NETP is selected" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(controllers.routes.BusinessEntityOverseasController.onSubmit().url)
          .post(Map("value" -> Seq(netpKey))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.AgriculturalFlatRateSchemeController.onPageLoad.url)

        verifySessionCacheData[BusinessEntity](sessionIdStr, BusinessEntityId, Some(NETP))
      }

      "return a redirect to Agricultural Flat Rate Scheme when NonUkCompany is selected" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(controllers.routes.BusinessEntityOverseasController.onSubmit().url)
          .post(Map("value" -> Seq(overseasKey))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.AgriculturalFlatRateSchemeController.onPageLoad.url)

        verifySessionCacheData[BusinessEntity](sessionIdStr, BusinessEntityId, Some(Overseas))
      }
    }
    "the user doesn't answer" must {
      "return BAD_REQUEST" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(controllers.routes.BusinessEntityOverseasController.onSubmit().url)
          .post(Map[String, String]()))

        res.status mustBe BAD_REQUEST
      }
    }
  }
}