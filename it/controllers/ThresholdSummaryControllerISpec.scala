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

import java.time.LocalDate

import common.enums.CacheKeys
import helpers.RequestsFinder
import models.view.{ExpectationOverThresholdView, OverThresholdView, Threshold, VoluntaryRegistration, VoluntaryRegistrationReason}
import models.view.VoluntaryRegistration._
import models.view.VoluntaryRegistrationReason._
import org.jsoup.Jsoup
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.http.HeaderNames
import play.api.libs.json.Json
import support.AppAndStubs

class ThresholdSummaryControllerISpec extends PlaySpec with AppAndStubs with RequestsFinder with ScalaFutures {
  val voluntaryRegistrationYES = VoluntaryRegistration(REGISTER_YES)
  val voluntaryRegistrationNO = VoluntaryRegistration(REGISTER_NO)
  val overThresholdTrue = OverThresholdView(true, Some(LocalDate.parse("2016-10-29")))
  val expectedOverThresholdTrue = ExpectationOverThresholdView(true, Some(LocalDate.parse("2016-11-25")))
  val overThresholdFalse = OverThresholdView(false, None)
  val expectedOverThresholdFalse = ExpectationOverThresholdView(false, None)
  val voluntaryRegistrationSELLS = VoluntaryRegistrationReason(SELLS)

  "GET Threshold Summary page" should {
    "return 200" when {
      "over threshold is false and Expected over threshold is false" in {
        val json = Json.parse(
          s"""
             |{
             |  "mandatoryRegistration": false,
             |  "voluntaryReason": "$SELLS"
             |}
           """.stripMargin)
        val s4lData = Threshold(None, Some(voluntaryRegistrationYES), Some(voluntaryRegistrationSELLS), Some(overThresholdFalse), Some(expectedOverThresholdFalse))

        given()
          .user.isAuthorised
          .currentProfile.withProfileAndIncorpDate()
          .audit.writesAudit()
          .s4lContainer.isEmpty
          .vatScheme.has("threshold", json)
          .s4lContainer.isUpdatedWith(CacheKeys.Threshold, s4lData)

        val response = buildClient("/check-confirm-threshold").get()
        whenReady(response) { res =>
          res.status mustBe 200
          val document = Jsoup.parse(res.body)
          document.title() mustBe "Check and confirm your answers"
          document.getElementById("threshold.overThresholdSelectionQuestion").text must include("05 August 2016")
          document.getElementById("threshold.overThresholdSelectionAnswer").text mustBe "No"
          document.getElementById("threshold.expectationOverThresholdSelectionAnswer").text mustBe "No"
        }
      }

      "over threshold is true with a date and Expected over threshold is true with a date" in {
        val json = Json.parse(
          s"""
             |{
             |  "mandatoryRegistration": true,
             |  "overThresholdDate": "2016-10-29",
             |  "expectedOverThresholdDate": "2016-11-25"
             |}
           """.stripMargin)
        val s4lData = Threshold(None, Some(voluntaryRegistrationNO), None, Some(overThresholdTrue), Some(expectedOverThresholdTrue))

          given()
          .user.isAuthorised
          .currentProfile.withProfileAndIncorpDate()
          .audit.writesAudit()
          .s4lContainer.isEmpty
          .vatScheme.has("threshold", json)
          .s4lContainer.isUpdatedWith(CacheKeys.Threshold, s4lData)

        val response = buildClient("/check-confirm-threshold").get()
        whenReady(response) { res =>
          res.status mustBe 200
          val document = Jsoup.parse(res.body)
          document.title() mustBe "Check and confirm your answers"
          document.getElementById("threshold.overThresholdSelectionQuestion").text must include ("05 August 2016")
          document.getElementById("threshold.overThresholdSelectionAnswer").text mustBe "Yes"
          document.getElementById("threshold.overThresholdDateAnswer").text mustBe "October 2016"
          document.getElementById("threshold.expectationOverThresholdSelectionAnswer").text mustBe "Yes"
          document.getElementById("threshold.expectationOverThresholdDateAnswer").text mustBe "25 November 2016"
        }
      }
    }

    "return an error page if the company is not incorporated" in {
      given()
        .user.isAuthorised
        .currentProfile.withProfile()
        .audit.writesAudit()

      val response = buildClient("/check-confirm-threshold").get()
      whenReady(response)(_.status) mustBe 500
    }
  }

  "POST Threshold Summary page" should {
    "return 303" when {
      "overThreshold is true with a date and expectedOverThreshold is false" in {
        val json = Json.parse(
          s"""
             |{
             |  "mandatoryRegistration": true,
             |  "overThresholdDate": "2016-10-29"
             |}
           """.stripMargin)
        val s4lData = Threshold(None, Some(voluntaryRegistrationNO), None, Some(overThresholdTrue), Some(expectedOverThresholdFalse))

        given()
          .user.isAuthorised
          .currentProfile.withProfileAndIncorpDate()
          .s4lContainer.isEmpty
          .vatScheme.has("threshold", json)
          .s4lContainer.isUpdatedWith(CacheKeys.Threshold, s4lData)
          .audit.writesAudit()

        val response = buildClient("/check-confirm-threshold").post(Map("" -> Seq("")))
        whenReady(response){ res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some("/vat-uri/who-is-registering-the-company-for-vat")
        }
      }

      "overThreshold is false and expectedOverThreshold is true with a date" in {
        val json = Json.parse(
          s"""
             |{
             |  "mandatoryRegistration": true,
             |  "expectedOverThresholdDate": "2016-11-25"
             |}
           """.stripMargin)
        val s4lData = Threshold(None, Some(voluntaryRegistrationNO), None, Some(overThresholdFalse), Some(expectedOverThresholdTrue))

        given()
          .user.isAuthorised
          .currentProfile.withProfileAndIncorpDate()
          .s4lContainer.isEmpty
          .vatScheme.has("threshold", json)
          .s4lContainer.isUpdatedWith(CacheKeys.Threshold, s4lData)
          .audit.writesAudit()

        val response = buildClient("/check-confirm-threshold").post(Map("" -> Seq("")))
        whenReady(response){ res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some("/vat-uri/who-is-registering-the-company-for-vat")
        }
      }

      "both overThreshold and expectedOverThreshold are false and redirect to Voluntary Registration page" in {
        val json = Json.parse(
          s"""
             |{
             |  "mandatoryRegistration": false,
             |  "voluntaryReason": "$SELLS"
             |}
           """.stripMargin)
        val s4lData = Threshold(None, Some(voluntaryRegistrationYES), Some(voluntaryRegistrationSELLS), Some(overThresholdFalse), Some(expectedOverThresholdFalse))

        given()
          .user.isAuthorised
          .currentProfile.withProfileAndIncorpDate()
          .s4lContainer.isEmpty
          .vatScheme.has("threshold", json)
          .s4lContainer.isUpdatedWith(CacheKeys.Threshold, s4lData)
          .audit.writesAudit()

        val response = buildClient("/check-confirm-threshold").post(Map("" -> Seq("")))
        whenReady(response){ res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VoluntaryRegistrationController.show().url)
        }
      }
    }
  }

}
