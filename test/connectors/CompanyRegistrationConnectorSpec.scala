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
import play.api.libs.json.{JsResultException, Json}
import uk.gov.hmrc.http.CoreGet

class CompanyRegistrationConnectorSpec extends ConnectorSpecBase {
  val regId = "test-regId"
  val compRegFakeUrl = "testUrl"
  val compRegStubbedFakeUrl = "stubbedTestUrl"

  class Setup(val crStubbed: Boolean = true) {
    val connector = new CompanyRegistrationConnector {
      override val http: CoreGet with WSHttp      = mockWSHttp
      override val companyRegistrationUrl: String = compRegFakeUrl
      override val useStub: Boolean               = crStubbed
      override val stubUrl: String                = compRegStubbedFakeUrl
    }
  }

  val stubbedFullUrl    = s"$compRegStubbedFakeUrl/incorporation-frontend-stub/$regId/corporation-tax-registration"
  val nonStubbedFullUrl = s"$compRegFakeUrl/company-registration/corporation-tax-registration/$regId/corporation-tax-registration"

  val compRegResponse = Json.parse(
    """
      | {
      |   "confirmationReferences" : {
      |     "transaction-id" : "foo"
      |   }
      | }
    """.stripMargin)

  "Calling getTransactionId" must {
    "return a transaction id using stubbed url" in new Setup {
      mockGet(stubbedFullUrl, compRegResponse)

      await(connector.getTransactionId(regId)) mustBe "foo"
    }
    "return a transaction id using non stubbed url" in new Setup(false) {
      mockGet(nonStubbedFullUrl, compRegResponse)

      await(connector.getTransactionId(regId)) mustBe "foo"
    }
    "return an exception if success response from CR but no transaction id in json" in new Setup {
      mockGet(stubbedFullUrl, Json.obj())

      intercept[JsResultException](await(connector.getTransactionId(regId)))
    }
    "return an exception if non 2xx response is returned from CR" in new Setup {
      mockFailedGet(stubbedFullUrl, new Exception("foo"))

      intercept[Exception](await(connector.getTransactionId(regId)))
    }
  }
}
