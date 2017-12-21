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

import common.enums.CacheKeys.IneligibilityReason
import common.enums.{CacheKeys, Version1EligibilityResult => ResultV1}
import helpers.RequestsFinder
import models.view.Eligibility
import org.jsoup.Jsoup
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.http._
import play.api.libs.json.{JsNumber, JsString, Json}
import support.AppAndStubs


class EligibilityControllerISpec extends PlaySpec with AppAndStubs with RequestsFinder with ScalaFutures {

  "Eligibility questions on GET" should {
    "return 200" when {
      "[Q1] /national-insurance-number The user is authorised, current prof is setup, vatscheme is blank,audit is successful, s4l 404's" in {
        given()
          .user.isAuthorised
          .currentProfile.withProfile()
          .s4lContainer.isEmpty
          .vatScheme.hasNoData("eligibility")
          .s4lContainer.isUpdatedWith(CacheKeys.Eligibility, Eligibility(None, None, None, None, None, None))
          .audit.writesAudit()
        val response = buildClient("/national-insurance-number").get()
        whenReady(response)(_.status) mustBe 200
      }
    }
    "return 200" when {
      "[Q2] /international-business the user is authorised, current prof is setup, vatscheme is blank, audit fails, s4l returns valid data" in {
        val s4lData = Eligibility(Some(true), Some(true), None, None, None, None)

        given()
          .user.isAuthorised
          .currentProfile.withProfile()
          .s4lContainer.contains(CacheKeys.Eligibility, s4lData)
          .audit.writesAudit()

        val response = buildClient("/international-business").get()
        whenReady(response) { res =>
          res.status mustBe 200
          val doc = Jsoup.parse(res.body)
          doc.title() mustBe "Will the company do international business once it's registered for VAT?"
          doc.getElementById("doingBusinessAbroadRadio-true").attr("checked") mustBe "checked"
        }
      }
    }
    "return 404" when {
      "[Q3] /involved-more-business-changing-status the user is not authorised to view the page but session is valid" in {
        given()
          .user.isNotAuthorised
          .audit.writesAudit()

        val response = buildClient("""/involved-more-business-changing-status"""").get()
        whenReady(response)(_.status) mustBe 404
      }
    }
    "return 303" when {
      "[Q4] /agricultural-flat-rate the user is authorised but the request is invalid" in {
        given()
          .user.isNotAuthorised
          .audit.writesAudit()

        val response = buildClient("/agricultural-flat-rate")(HeaderNames.COOKIE -> "foo").get()
        whenReady(response)(_.status) mustBe 303
      }
    }
    "return 200" when {
      "[Q5] /apply-exception-exemption  the user is authorised, current prof is setup, vatscheme is blank, s4l returns valid data, prepop field" in {

        val s4lData = Eligibility(hasNino = Some(true),
          internationalBusiness = Some(true),
          moreThanOneBusinessOrLegalStatus = Some(false),
          agriculturalFRSOrAnnualAccount = Some(false),
          exceptionOrExemption = Some(true),
          racehorsesOrLandAndProperty = None
        )

        given()
          .user.isAuthorised
          .currentProfile.withProfile()
          .s4lContainer.contains(CacheKeys.Eligibility, s4lData)
          .audit.writesAudit()

        val response = buildClient("/apply-exception-exemption").get()
        whenReady(response) { res =>
          res.status mustBe 200
          val document = Jsoup.parse(res.body)
          document.title() mustBe "Will the company apply for a VAT registration exception or exemption?"
          document.getElementById("applyingForVatExemptionRadio-true").attr("checked") mustBe "checked"
        }
      }
    }
    "return 200" when {
      "[Q6] /apply-for-any the user is authorised and data is in vat reg backend. data is pulled from vat because S4l returns 404" in {
        val json = Json.parse(
          s"""
             |{
             |  "version": 1,
             |  "result": "success"
             |}
           """.stripMargin)
        val s4lData = Eligibility(hasNino = Some(true),
          internationalBusiness = Some(true),
          moreThanOneBusinessOrLegalStatus = Some(false),
          agriculturalFRSOrAnnualAccount = Some(false),
          exceptionOrExemption = Some(false),
          racehorsesOrLandAndProperty = Some(false)
        )

        given()
          .user.isAuthorised
          .currentProfile.withProfile()
          .audit.writesAudit()
          .s4lContainer.isEmpty
          .vatScheme.has("eligibility", json)
          .s4lContainer.isUpdatedWith(CacheKeys.Eligibility, s4lData)

        val response = buildClient("/apply-for-any").get()
        whenReady(response) { res =>
          res.status mustBe 200
          Jsoup.parse(res.body).getElementById("companyWillDoAnyOfRadio-false").attr("checked") mustBe "checked"
        }
      }
    }
  }

  "Eligibility questions on POST" should {
    "return 303 and redirects to next page of questions" when {
      "[Q1] /national-insurance-number the user submits an answer" in {
        val s4lData = Eligibility(Some(false), None, None, None, None, None)
        val s4lDataUpdated = Eligibility(Some(true), None, None, None, None, None)

        given()
          .user.isAuthorised
          .currentProfile.withProfile()
          .audit.writesAudit()
          .s4lContainer.contains(CacheKeys.Eligibility, s4lData)
          .s4lContainer.isUpdatedWith(CacheKeys.Eligibility, s4lDataUpdated)

        val response = buildClient("/national-insurance-number").post(Map("haveNinoRadio" -> Seq("true")))
        whenReady(response) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some("/check-if-you-can-register-for-vat/international-business")
        }
      }
    }
    "return 303 and redirects to next page of you are not eligible" when {
      "[Q1] /national-insurance-number the user submits an answer which is false" in {
        val s4lData = Eligibility(Some(true), Some(true), None, None, None, None)

        val json = Json.parse(
          s"""
             |{
             |  "version": 1,
             |  "result": "${ResultV1.noNino}"
             |}
           """.stripMargin)

        given()
          .user.isAuthorised
          .currentProfile.withProfile()
          .audit.writesAudit()
          .s4lContainer.contains(CacheKeys.Eligibility, s4lData)
          .vatScheme.patched("eligibility", json)
          .s4lContainer.cleared
          .keystore.putKeyStoreValue(IneligibilityReason, """"haveNino"""")

        val response = buildClient("/national-insurance-number").post(Map("haveNinoRadio" -> Seq("false")))
        whenReady(response) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some("/check-if-you-can-register-for-vat/cant-register")
        }
      }
    }
    "return 400 when invalid form is submitted" in {
      val s4lData = Eligibility(None, None, None, None, None, None)

      given()
        .user.isAuthorised
        .currentProfile.withProfile()
        .s4lContainer.contains(CacheKeys.Eligibility, s4lData)
        .audit.writesAudit()

      val response = buildClient("/national-insurance-number").post(Map("haveNinoRadio" -> Seq("fooBars")))
      whenReady(response)(_.status) mustBe 400
    }

    "return 303 and save into backend when /apply-for-any final question is submitted successfully" in {
      val s4lData = Eligibility(hasNino = Some(true),
        internationalBusiness = Some(false),
        moreThanOneBusinessOrLegalStatus = Some(false),
        agriculturalFRSOrAnnualAccount = Some(false),
        exceptionOrExemption = Some(false),
        racehorsesOrLandAndProperty = None
      )
      val json = Json.parse(
        s"""
           |{
           |  "version": 1,
           |  "result": "success"
           |}
         """.stripMargin)

      given()
        .user.isAuthorised
        .currentProfile.withProfileAndIncorpDate()
        .audit.writesAudit()
        .s4lContainer.contains(CacheKeys.Eligibility, s4lData)
        .vatScheme.patched("eligibility", json)
        .s4lContainer.cleared

      val response = buildClient("/apply-for-any").post(Map("companyWillDoAnyOfRadio" -> Seq("false")))
      whenReady(response) {
        res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some("/check-if-you-can-register-for-vat/check-confirm-eligibility")

          val json = getPATCHRequestJsonBody(s"/vatreg/1/eligibility")
          (json \ "version").as[JsNumber].value mustBe 1
          (json \ "result").as[JsString].value mustBe "success"
      }
    }
  }
}
