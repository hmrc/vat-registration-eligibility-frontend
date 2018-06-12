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

import base.ConnectorSpecBase
import config.WSHttp
import play.api.http.Status.OK
import play.api.libs.json.{JsResultException, Json}
import uk.gov.hmrc.http.{CoreGet, NotFoundException}

class BusinessRegistrationConnectorSpec extends ConnectorSpecBase {
  val regId = "test-regId"
  val fakeUrl = "testUrl"

  "Calling getBusinessRegistrationId" must {
    val connector = new BusinessRegistrationConnector {
      override val businessRegistrationUrl: String = fakeUrl
      override val http: CoreGet with WSHttp = mockWSHttp
    }

    "return a registration id" in {
      val json = Json.parse(
        s"""
           |{
           |  "registrationID": "$regId"
           |}
         """.stripMargin)

      mockGet(s"$fakeUrl/business-registration/business-tax-registration", OK, Some(json))

      await(connector.getBusinessRegistrationId) mustBe regId
      verifyGetCalled("testUrl/business-registration/business-tax-registration")
    }

    "return none" when {
      "the json is incorrect" in {
        val jsonIncorrect = Json.parse(
          s"""
             |{
             |  "wrongKey": "$regId"
             |}
         """.stripMargin)

        mockGet(s"$fakeUrl/business-registration/business-tax-registration", OK, Some(jsonIncorrect))

        a[JsResultException] mustBe thrownBy(await(connector.getBusinessRegistrationId))
        verifyGetCalled("testUrl/business-registration/business-tax-registration")
      }
      "it is not found" in {
        mockFailedGet(s"$fakeUrl/business-registration/business-tax-registration", new NotFoundException("not found"))

        a[NotFoundException] mustBe thrownBy(await(connector.getBusinessRegistrationId))
        verifyGetCalled("testUrl/business-registration/business-tax-registration")
      }
      "there is an error" in {
        mockFailedGet(s"$fakeUrl/business-registration/business-tax-registration", new Exception("some error"))
        an[Exception] mustBe thrownBy (await(connector.getBusinessRegistrationId))
        verifyGetCalled("testUrl/business-registration/business-tax-registration")
      }
    }
  }
}
