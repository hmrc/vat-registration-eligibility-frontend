/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.http.Status.OK
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.ExecutionContext.Implicits.global

class VatRegistrationConnectorSpec extends ConnectorSpecBase {
  override val regId = "test-regId"
  val fakeUrl = "testUrl"
  val fakeUri = "testUri"

  val jsonBlock = Json.toJson("""{}""")

  "Calling saveEligibility" must {
    val connector = new VatRegistrationConnector(http = mockHttpClient, servicesConfig = mock[ServicesConfig]) {
      override lazy val vatRegistrationUrl: String = fakeUrl
      override lazy val vatRegistrationUri: String = fakeUri
    }

    "save the eligibility block to backend" in {
      val json = Json.parse(
        s"""
           |{
           |  "registrationID": "$regId"
           |}
         """.stripMargin)

      mockPatch[JsValue](s"$fakeUrl$fakeUri/$regId/eligibility-data", OK, Some(json))

      await(connector.saveEligibility(regId, jsonBlock)) mustBe json
      verifyPatchCalled[JsValue, HttpResponse](s"$fakeUrl$fakeUri/$regId/eligibility-data")
    }
  }
}