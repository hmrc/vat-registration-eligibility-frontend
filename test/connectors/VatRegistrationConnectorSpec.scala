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

package connectors

import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.external.IncorporationInfo
import play.api.http.Status._
import config.WSHttp
import models.view.VoluntaryRegistrationReason.SELLS
import play.api.libs.json.{JsValue, Json}

import scala.language.postfixOps
import uk.gov.hmrc.http.{HttpResponse, NotFoundException, Upstream4xxResponse, Upstream5xxResponse}

class VatRegistrationConnectorSpec extends VatRegSpec with VatRegistrationFixture {
  class Setup {
    val connector = new VatRegistrationConnector {
      override val vatRegUrl: String = "tst-url"
      override val http: WSHttp = mockWSHttp
    }
  }

  val forbidden = Upstream4xxResponse(FORBIDDEN.toString, FORBIDDEN, FORBIDDEN)
  val internalServerError = Upstream5xxResponse(INTERNAL_SERVER_ERROR.toString, INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)
  val notFound = new NotFoundException(NOT_FOUND.toString)
  val exception = new Exception(BAD_GATEWAY.toString)

  "Calling getIncorporationInfo" should {

    "return a IncorporationInfo when it can be retrieved from the microservice" in new Setup {
      mockHttpGET[IncorporationInfo]("tst-url", testIncorporationInfo)
      await(connector.getIncorporationInfo("tstID")) shouldBe Some(testIncorporationInfo)
    }

    "fail when an Internal Server Error response is returned by the microservice" in new Setup {
      mockHttpFailedGET[IncorporationInfo]("test-url", notFound)
      await(connector.getIncorporationInfo("tstID")) shouldBe None
    }
  }

  "Calling getEligibility" should {
    val validJson = Json.parse(
      s"""
         |{
         |  "version": 1,
         |  "result": "success"
         |}
       """.stripMargin)
    val httpRespOK = HttpResponse(OK, Some(validJson))
    val httpRespNOCONTENT = HttpResponse(NO_CONTENT, None)

    "return a tuple2 (String, Int)" in new Setup {
      mockHttpGET[HttpResponse]("tst-url", httpRespOK)
      connector.getEligibility returns Some(("success", 1))
    }

    "return None if there is no data for the registration" in new Setup {
      mockHttpGET[HttpResponse]("tst-url", httpRespNOCONTENT)
      connector.getEligibility returns None
    }

    "throw a NotFoundException if the registration does not exist" in new Setup {
      mockHttpFailedGET[HttpResponse]("tst-url", notFound)
      connector.getEligibility failedWith notFound
    }

    "throw an Upstream4xxResponse with Forbidden status" in new Setup {
      mockHttpFailedGET[HttpResponse]("tst-url", forbidden)
      connector.getEligibility failedWith forbidden
    }

    "throw an Upstream5xxResponse with Internal Server Error status" in new Setup {
      mockHttpFailedGET[HttpResponse]("tst-url", internalServerError)
      connector.getEligibility failedWith internalServerError
    }

    "throw an Exception if the call failed" in new Setup {
      mockHttpFailedGET[HttpResponse]("tst-url", exception)
      connector.getEligibility failedWith exception
    }
  }

  "Calling patchEligibility" should {
    val validJson = Json.parse(
      s"""
         |{
         |  "version": 1,
         |  "result": "success"
         |}
       """.stripMargin)

    "return a JsValue" in new Setup {
      mockHttpPATCH[JsValue, JsValue]("tst-url", validJson)
      connector.patchEligibility("success", 1) returns validJson
    }

    "throw a NotFoundException if the registration does not exist" in new Setup {
      mockHttpFailedPATCH[JsValue, JsValue]("tst-url", notFound)
      connector.patchEligibility("success", 1) failedWith notFound
    }

    "throw an Upstream4xxResponse with Forbidden status" in new Setup {
      mockHttpFailedPATCH[JsValue, JsValue]("tst-url", forbidden)
      connector.patchEligibility("success", 1) failedWith forbidden
    }

    "throw an Upstream5xxResponse with Internal Server Error status" in new Setup {
      mockHttpFailedPATCH[JsValue, JsValue]("tst-url", internalServerError)
      connector.patchEligibility("success", 1) failedWith internalServerError
    }

    "throw an Exception if the call failed" in new Setup {
      mockHttpFailedPATCH[JsValue, JsValue]("tst-url", exception)
      connector.patchEligibility("success", 1) failedWith exception
    }
  }

  "Calling getThreshold" should {
    val validJson = Json.parse(
      s"""
         |{
         |  "mandatoryRegistration": true
         |}
       """.stripMargin)
    val httpRespOK = HttpResponse(OK, Some(validJson))
    val httpRespNOCONTENT = HttpResponse(NO_CONTENT, None)

    "return a JsValue" in new Setup {
      mockHttpGET[HttpResponse]("tst-url", httpRespOK)
      connector.getThreshold returns Some(validJson)
    }

    "return None if there is no data for the registration" in new Setup {
      mockHttpGET[HttpResponse]("tst-url", httpRespNOCONTENT)
      connector.getThreshold returns None
    }

    "throw a NotFoundException if the registration does not exist" in new Setup {
      mockHttpFailedGET[HttpResponse]("tst-url", notFound)
      connector.getThreshold failedWith notFound
    }

    "throw an Upstream4xxResponse with Forbidden status" in new Setup {
      mockHttpFailedGET[HttpResponse]("tst-url", forbidden)
      connector.getThreshold failedWith forbidden
    }

    "throw an Upstream5xxResponse with Internal Server Error status" in new Setup {
      mockHttpFailedGET[HttpResponse]("tst-url", internalServerError)
      connector.getThreshold failedWith internalServerError
    }

    "throw an Exception if the call failed" in new Setup {
      mockHttpFailedGET[HttpResponse]("tst-url", exception)
      connector.getThreshold failedWith exception
    }
  }

  "Calling patchThreshold" should {
    val validJson = Json.parse(
      s"""
         |{
         |  "mandatoryRegistration": false,
         |  "voluntaryReason": "$SELLS"
         |}
       """.stripMargin)

    "return a JsValue" in new Setup {
      mockHttpPATCH[JsValue, JsValue]("tst-url", validJson)
      connector.patchThreshold(validThresholdPreIncorp) returns validJson
    }

    "throw a NotFoundException if the registration does not exist" in new Setup {
      mockHttpFailedPATCH[JsValue, JsValue]("tst-url", notFound)
      connector.patchThreshold(validThresholdPreIncorp) failedWith notFound
    }

    "throw an Upstream4xxResponse with Forbidden status" in new Setup {
      mockHttpFailedPATCH[JsValue, JsValue]("tst-url", forbidden)
      connector.patchThreshold(validThresholdPreIncorp) failedWith forbidden
    }

    "throw an Upstream5xxResponse with Internal Server Error status" in new Setup {
      mockHttpFailedPATCH[JsValue, JsValue]("tst-url", internalServerError)
      connector.patchThreshold(validThresholdPreIncorp) failedWith internalServerError
    }

    "throw an Exception if the call failed" in new Setup {
      mockHttpFailedPATCH[JsValue, JsValue]("tst-url", exception)
      connector.patchThreshold(validThresholdPreIncorp) failedWith exception
    }
  }
}
