/*
 * Copyright 2018 HM Revenue & Customs
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

import mocks.VatMocks
import models.external.CompanyRegistrationProfile
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsObject, Json}
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import utils.VREFEFeatureSwitches
import org.mockito.Mockito.when
import scala.concurrent.Future

class CompanyRegistrationConnectorSpec extends UnitSpec with MockitoSugar with VatMocks {

  val testUrl = "testUrl"
  val testUri = "testUri"
  val mockFeatureSwitch = mock[VREFEFeatureSwitches]

  implicit val hc = HeaderCarrier()

  class Setup(stubbed: Boolean) {
    val connector = new CompanyRegistrationConnector {
      override val companyRegistrationUri = testUri
      override val companyRegistrationUrl = testUrl
      override lazy val stubUri = testUri
      override lazy val stubUrl = testUrl
      override val http = mockWSHttp
      override def useCompanyRegistration = stubbed
      override val featureSwitch = mockFeatureSwitch
      override lazy val config = mockAppConfig
    }
      when(mockAppConfig.whitelistedRegIds).thenReturn(Seq("foo"))
  }

  val status = "submitted"
  val transactionId = "submitted"

  val profileJson =
    Json.parse(
      s"""
        |{
        |    "registration-id" : "testRegId",
        |    "status" : "$status",
        |    "confirmationReferences" : {
        |       "acknowledgement-reference" : "BRCT-0123456789",
        |       "transaction-id" : "$transactionId"
        |    }
        |}
      """.stripMargin).as[JsObject]

  val profileJsonMin =
    Json.parse(
      s"""
        |{
        |    "registration-id" : "testRegId",
        |    "status" : "$status"
        |}
      """.stripMargin).as[JsObject]

  "getCompanyRegistrationDetails" should {
    "return a CompanyProfile" in new Setup(false) {
      mockHttpGET[JsObject](connector.companyRegistrationUri, Future.successful(profileJson))

      val result = await(connector.getCompanyRegistrationDetails("testRegId"))
      result shouldBe CompanyRegistrationProfile(status, transactionId)
    }

    "throw a bad request exception" in new Setup(false) {
      mockHttpGET[JsObject](connector.companyRegistrationUri, Future.failed(new BadRequestException("tstException")))

      intercept[BadRequestException](await(connector.getCompanyRegistrationDetails("testRegId")))
    }

    "throw any other exception" in new Setup(false) {
      mockHttpGET[JsObject](connector.companyRegistrationUri, Future.failed(new RuntimeException))

      intercept[RuntimeException](await(connector.getCompanyRegistrationDetails("testRegId")))
    }

    "be stubbed" when {
      "returning a CompanyProfile" in new Setup(false) {
        mockHttpGET[JsObject](connector.companyRegistrationUri, Future.successful(profileJson))

        val result = await(connector.getCompanyRegistrationDetails("testRegId"))
        result shouldBe CompanyRegistrationProfile(status, transactionId)
      }

      "throwing a bad request exception" in new Setup(false) {
        mockHttpGET[JsObject](connector.companyRegistrationUri, Future.failed(new BadRequestException("tstException")))

        intercept[BadRequestException](await(connector.getCompanyRegistrationDetails("testRegId")))
      }

      "throwing any other exception" in new Setup(false) {
        mockHttpGET[JsObject](connector.companyRegistrationUri, Future.failed(new RuntimeException))

        intercept[RuntimeException](await(connector.getCompanyRegistrationDetails("testRegId")))
      }
    }
  }
}
