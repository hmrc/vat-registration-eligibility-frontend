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

import common.enums.{CacheKeys, VatRegStatus}
import helpers.RequestsFinder
import models.view.Eligibility
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.http.HeaderNames
import play.api.libs.json.Json
import support.AppAndStubs

class EligibilitySummaryControllerISpec extends PlaySpec with AppAndStubs with RequestsFinder with ScalaFutures {
  val json = Json.parse(
    s"""
       |{
       |  "version": 1,
       |  "result": "success"
       |}
     """.stripMargin)

  val s4lData = Json.toJson(Eligibility(
    Some(true),
    Some(false),
    Some(false),
    Some(false),
    Some(false),
    Some(false)
  ))

  def validateEligibilitySummaryPage(document: Document) = {
    document.title() mustBe "Check and confirm your answers"
    document.getElementById("nationalInsurance.hasNinoAnswer").text mustBe "Yes"
    document.getElementById("nationalInsurance.hasNinoChangeLink").attr("href") mustBe controllers.routes.EligibilityController.showHaveNino().url

    document.getElementById("internationalBusiness.sellGoodsAnswer").text mustBe "No"
    document.getElementById("internationalBusiness.buyGoodsAnswer").text mustBe "No"
    document.getElementById("internationalBusiness.sellAssetsAnswer").text mustBe "No"
    document.getElementById("internationalBusiness.sellGoodsServicesAnswer").text mustBe "No"
    document.getElementById("internationalBusiness.doBusinessAnswer").text mustBe "No"
    document.getElementById("internationalBusiness.sellGoodsChangeLink").attr("href") mustBe controllers.routes.EligibilityController.showDoingBusinessAbroad().url
    document.getElementById("internationalBusiness.buyGoodsChangeLink").attr("href") mustBe controllers.routes.EligibilityController.showDoingBusinessAbroad().url
    document.getElementById("internationalBusiness.sellAssetsChangeLink").attr("href") mustBe controllers.routes.EligibilityController.showDoingBusinessAbroad().url
    document.getElementById("internationalBusiness.sellGoodsServicesChangeLink").attr("href") mustBe controllers.routes.EligibilityController.showDoingBusinessAbroad().url
    document.getElementById("internationalBusiness.doBusinessChangeLink").attr("href") mustBe controllers.routes.EligibilityController.showDoingBusinessAbroad().url

    document.getElementById("otherBusiness.soleTraderAnswer").text mustBe "No"
    document.getElementById("otherBusiness.vatGroupAnswer").text mustBe "No"
    document.getElementById("otherBusiness.makingProfitAnswer").text mustBe "No"
    document.getElementById("otherBusiness.limitedCompanyAnswer").text mustBe "No"
    document.getElementById("otherBusiness.soleTraderChangeLink").attr("href") mustBe controllers.routes.EligibilityController.showDoAnyApplyToYou().url
    document.getElementById("otherBusiness.vatGroupChangeLink").attr("href") mustBe controllers.routes.EligibilityController.showDoAnyApplyToYou().url
    document.getElementById("otherBusiness.makingProfitChangeLink").attr("href") mustBe controllers.routes.EligibilityController.showDoAnyApplyToYou().url
    document.getElementById("otherBusiness.limitedCompanyChangeLink").attr("href") mustBe controllers.routes.EligibilityController.showDoAnyApplyToYou().url

    document.getElementById("otherVatScheme.agriculturalFlatAnswer").text mustBe "No"
    document.getElementById("otherVatScheme.accountingSchemeAnswer").text mustBe "No"
    document.getElementById("otherVatScheme.agriculturalFlatChangeLink").attr("href") mustBe controllers.routes.EligibilityController.showApplyingForAnyOf().url
    document.getElementById("otherVatScheme.accountingSchemeChangeLink").attr("href") mustBe controllers.routes.EligibilityController.showApplyingForAnyOf().url

    document.getElementById("vatExemption.vatExceptionAnswer").text mustBe "No"
    document.getElementById("vatExemption.vatExemptionAnswer").text mustBe "No"
    document.getElementById("vatExemption.vatExceptionChangeLink").attr("href") mustBe controllers.routes.EligibilityController.showExemptionCriteria().url
    document.getElementById("vatExemption.vatExemptionChangeLink").attr("href") mustBe controllers.routes.EligibilityController.showExemptionCriteria().url

    document.getElementById("resources.companyOwnAnswer").text mustBe "No"
    document.getElementById("resources.companySellAnswer").text mustBe "No"
    document.getElementById("resources.companyOwnChangeLink").attr("href") mustBe controllers.routes.EligibilityController.showCompanyWillDoAnyOf().url
    document.getElementById("resources.companySellChangeLink").attr("href") mustBe controllers.routes.EligibilityController.showCompanyWillDoAnyOf().url
  }

  "GET Eligibility Summary page" should {
    "return 200" when {
      "page is rendered with all data passed in if data is in s4l" in {

        given()
          .user.isAuthorised
          .currentProfile.withProfile()
          .vatScheme.isBlank
          .audit.writesAudit()
          .s4lContainer.contains(CacheKeys.Eligibility, s4lData)

        val response = buildClient("/check-confirm-eligibility").get()
        whenReady(response) { res =>
          res.status mustBe 200
          val document = Jsoup.parse(res.body)
          validateEligibilitySummaryPage(document)
        }
      }
      "page is rendered with all data passed in if data is in backend" in {
        val s4lData = Eligibility(
          Some(true),
          Some(false),
          Some(false),
          Some(false),
          Some(false),
          Some(false)
        )
        given()
          .user.isAuthorised
          .currentProfile.withProfile()
          .audit.writesAudit()
          .s4lContainer.isEmpty
          .vatScheme.has("eligibility", json)
          .s4lContainer.isUpdatedWith(CacheKeys.Eligibility, s4lData)

        val response = buildClient("/check-confirm-eligibility").get()
        whenReady(response) { res =>
          res.status mustBe 200
          val document = Jsoup.parse(res.body)
          validateEligibilitySummaryPage(document)
        }
      }
    }
  }
  "Save and continue on eligibility summary for an incorporated company" should {
    "redirect to threshold page" in {
      given()
        .user.isAuthorised
        .currentProfile.withProfileAndIncorpDate()
        .audit.writesAudit()
        .s4lContainer.contains(CacheKeys.Eligibility, s4lData)

      val response = buildClient("/check-confirm-eligibility").post(Map("" -> Seq("")))
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdController.goneOverShow().url)
      }
    }
  }

  "Save and continue on eligibility summary for a none incorporated company" should {
    "redirect to taxable turnover page" in {
      val profile = Json.parse(s"""
                      |{
                      | "companyName" : "testCompanyName",
                      | "registrationID" : "1",
                      | "transactionID" : "000-434-1",
                      | "vatRegistrationStatus" : "${VatRegStatus.draft}"
                      |}
                    """.stripMargin)
      given()
        .user.isAuthorised
        .currentProfile.withProfile()
        .company.incorporationStatusNotKnown()
        .keystore.putKeyStoreValue(CacheKeys.CurrentProfile, profile.toString)
        .audit.writesAudit()
        .s4lContainer.contains(CacheKeys.Eligibility, s4lData)

      val response = buildClient("/check-confirm-eligibility").post(Map("" -> Seq("")))
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaxableTurnoverController.show().url)
      }
    }
  }
}
