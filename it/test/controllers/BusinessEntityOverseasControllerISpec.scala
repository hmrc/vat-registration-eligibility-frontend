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
import models.BusinessEntity.{generalPartnershipKey, limitedLiabilityPartnershipKey, limitedPartnershipKey, nonIncorporatedTrustKey, overseasKey, partnershipKey, soleTraderKey, ukCompanyKey}
import models.{BusinessEntity, GeneralPartnership, LimitedLiabilityPartnership, LimitedPartnership, NonIncorporatedTrust, Overseas, Partnership, SoleTrader, UKCompany}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class BusinessEntityOverseasControllerISpec extends IntegrationSpecBase {

  val pageUrl = "/business-entity-overseas"

  "GET /business-entity-overseas" when {
    "an answer already exists for the page" must {
      "return OK with the answer pre-populated" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        cacheSessionData[BusinessEntity](sessionIdStr, BusinessEntityId, SoleTrader)

        val res: WSResponse = await(buildClient(pageUrl).get)
        val doc: Document = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.radioIsSelected(soleTraderKey) mustBe true
        doc.radioIsSelected(overseasKey) mustBe false
        doc.radioIsSelected(ukCompanyKey) mustBe false
        doc.radioIsSelected(generalPartnershipKey) mustBe false
        doc.radioIsSelected(limitedLiabilityPartnershipKey) mustBe false
        doc.radioIsSelected(nonIncorporatedTrustKey) mustBe false
      }
    }
    "an answer doesn't exist for the page" must {
      "return OK with an empty form" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res: WSResponse = await(buildClient(pageUrl).get)
        val doc: Document = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.radioIsSelected(soleTraderKey) mustBe false
        doc.radioIsSelected(overseasKey) mustBe false
        doc.radioIsSelected(ukCompanyKey) mustBe false
        doc.radioIsSelected(generalPartnershipKey) mustBe false
        doc.radioIsSelected(limitedLiabilityPartnershipKey) mustBe false
        doc.radioIsSelected(nonIncorporatedTrustKey) mustBe false
      }
    }
  }

  s"POST /business-entity-overseas" when {
    "the user answers" must {
      "return a redirect to Agricultural Flat Rate Scheme when Sole Trader is selected" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res: WSResponse = await(buildClient(controllers.routes.BusinessEntityOverseasController.onSubmit().url)
          .post(Map("value" -> Seq(soleTraderKey))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.AgriculturalFlatRateSchemeController.onPageLoad.url)

        verifySessionCacheData[BusinessEntity](sessionIdStr, BusinessEntityId, Some(SoleTrader))
      }

      "return a redirect to Agricultural Flat Rate Scheme when NonUkCompany is selected" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res: WSResponse = await(buildClient(controllers.routes.BusinessEntityOverseasController.onSubmit().url)
          .post(Map("value" -> Seq(overseasKey))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.AgriculturalFlatRateSchemeController.onPageLoad.url)

        verifySessionCacheData[BusinessEntity](sessionIdStr, BusinessEntityId, Some(Overseas))
      }

      "return a redirect to Agricultural Flat Rate Scheme when Uk Company is selected" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res: WSResponse = await(buildClient(controllers.routes.BusinessEntityOverseasController.onSubmit().url)
          .post(Map("value" -> Seq(ukCompanyKey))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.AgriculturalFlatRateSchemeController.onPageLoad.url)

        verifySessionCacheData[BusinessEntity](sessionIdStr, BusinessEntityId, Some(UKCompany))
      }

      "return a redirect to Agricultural Flat Rate Scheme when General Partnership is selected" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res: WSResponse = await(buildClient(controllers.routes.BusinessEntityOverseasController.onSubmit().url)
          .post(Map("value" -> Seq(generalPartnershipKey))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.AgriculturalFlatRateSchemeController.onPageLoad.url)

        verifySessionCacheData[BusinessEntity](sessionIdStr, BusinessEntityId, Some(GeneralPartnership))
      }

      "return a redirect to Agricultural Flat Rate Scheme when Limited Partnership is selected" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res: WSResponse = await(buildClient(controllers.routes.BusinessEntityOverseasController.onSubmit().url)
          .post(Map("value" -> Seq(limitedLiabilityPartnershipKey))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.AgriculturalFlatRateSchemeController.onPageLoad.url)

        verifySessionCacheData[BusinessEntity](sessionIdStr, BusinessEntityId, Some(LimitedLiabilityPartnership))
      }

      "return a redirect to Agricultural Flat Rate Scheme when Non Incorporated Trust is selected" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res: WSResponse = await(buildClient(controllers.routes.BusinessEntityOverseasController.onSubmit().url)
          .post(Map("value" -> Seq(nonIncorporatedTrustKey))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.AgriculturalFlatRateSchemeController.onPageLoad.url)

        verifySessionCacheData[BusinessEntity](sessionIdStr, BusinessEntityId, Some(NonIncorporatedTrust))
      }
    }

    "the user doesn't answer" must {
      "return BAD_REQUEST" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res: WSResponse = await(buildClient(controllers.routes.BusinessEntityOverseasController.onSubmit().url)
          .post(Map[String, String]()))

        res.status mustBe BAD_REQUEST
      }
    }
  }
}