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

import java.time.LocalDate

import base.ConnectorSpecBase
import config.WSHttp
import play.api.libs.json.Json
import uk.gov.hmrc.http.{CoreGet, InternalServerException}

class IncorporationInformationConnectorSpec extends ConnectorSpecBase {

  class Setup {
    val fakeUrl = "testUrl"
    val connector = new IncorporationInformationConnector {
      override val http: CoreGet with WSHttp = mockWSHttp
      override val incorpInfoUrl: String = fakeUrl
      override val incorpInfoUri: String = ""
    }
  }

  val txId      = "someTxId"

  val validIncorpData = Json.parse(
    s"""
      |{
      | "transaction_id" : "$txId",
      | "status" : "accepted",
      | "crn" : "OC12374C",
      | "incorporationDate" : "${LocalDate.now}",
      | "timepoint" : "12334545553"
      |}
    """.stripMargin)

  "getIncorpData" should {
    "return some data" in new Setup {
      mockGet(s"$fakeUrl/incorp-update", 200, Some(validIncorpData))
      await(connector.getIncorpData(txId)) mustBe Some(validIncorpData)
      verifyGetCalled("testUrl")
    }

    "return none if not data present" in new Setup {
      mockGet(s"$fakeUrl/incorp-update", 204, Some(validIncorpData))
      await(connector.getIncorpData(txId)) mustBe None
      verifyGetCalled("testUrl")
    }

    "throw and exception if occurs" in new Setup {
      mockFailedGet(s"$fakeUrl/incorp-update", new InternalServerException("internal server error"))
      intercept[InternalServerException](await(connector.getIncorpData(txId)))
    }
  }
}
