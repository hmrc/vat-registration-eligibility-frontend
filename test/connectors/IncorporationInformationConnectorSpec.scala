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
import play.api.http.Status._
import uk.gov.hmrc.http.{CoreGet, InternalServerException, NotFoundException}

class IncorporationInformationConnectorSpec extends ConnectorSpecBase {

  val txId      = "someTxId"

  class Setup {
    val fakeUrl = "testUrl"
    val connector = new IncorporationInformationConnector {
      override val http: CoreGet with WSHttp = mockWSHttp
      override val incorpInfoUrl: String = fakeUrl
      override val incorpInfoUri: String = ""
    }

    val fetchIncorpDataUrl = s"$fakeUrl/$txId/incorporation-update"
    val fetchOfficerUrl = s"$fakeUrl/$txId/officer-list"
  }

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

  val tstOfficerListJson = Json.parse(
    """
      |{
      |  "officers": [
      |    {
      |      "name" : "test",
      |      "name_elements" : {
      |        "forename" : "test1",
      |        "other_forenames" : "test11",
      |        "surname" : "testa",
      |        "title" : "Mr"
      |      },
      |      "officer_role" : "cic-manager"
      |    }, {
      |      "name" : "test",
      |      "name_elements" : {
      |        "forename" : "test2",
      |        "other_forenames" : "test22",
      |        "surname" : "testb",
      |        "title" : "Mr"
      |      },
      |      "officer_role" : "corporate-director"
      |    }
      |  ]
      |}""".stripMargin)

  "getIncorpData" should {
    "return some data" in new Setup {
      mockGet(fetchIncorpDataUrl, OK, Some(validIncorpData))
      await(connector.getIncorpData(txId)) mustBe Some(validIncorpData)
      verifyGetCalled(fetchIncorpDataUrl)
    }

    "return none if not data present" in new Setup {
      mockGet(fetchIncorpDataUrl, NO_CONTENT, Some(validIncorpData))
      await(connector.getIncorpData(txId)) mustBe None
      verifyGetCalled(fetchIncorpDataUrl)
    }

    "throw and exception if occurs" in new Setup {
      mockFailedGet(fetchIncorpDataUrl, new InternalServerException("internal server error"))
      intercept[InternalServerException](await(connector.getIncorpData(txId)))
      verifyGetCalled(fetchIncorpDataUrl)
    }
  }

  "getOfficerList" should {
    "return some data" in new Setup {
      mockGet(fetchOfficerUrl, OK, Some(tstOfficerListJson))
      await(connector.getOfficerList(txId)) mustBe tstOfficerListJson
      verifyGetCalled(fetchOfficerUrl)
    }

    "throw exception if not data return form ii" in new Setup {
      mockFailedGet(fetchOfficerUrl, new NotFoundException("internal server error"))
      intercept[NotFoundException](await(connector.getOfficerList(txId)))
      verifyGetCalled(fetchOfficerUrl)
    }

    "throw and exception if occurs" in new Setup {
      mockFailedGet(fetchOfficerUrl, new InternalServerException("internal server error"))
      intercept[InternalServerException](await(connector.getOfficerList(txId)))
      verifyGetCalled(fetchOfficerUrl)
    }
  }
}
