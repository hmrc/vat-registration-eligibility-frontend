/*
 * Copyright 2024 HM Revenue & Customs
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

import helpers.IntegrationSpecBase
import play.api.http.Status.OK
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global

class VatRegistrationConnectorISpec extends IntegrationSpecBase {

  val connector = app.injector.instanceOf[VatRegistrationConnector]
  val apiUrl = s"/vatreg/$testRegId/eligibility-data"

  val testJson = Json.obj(
    "sections" -> Json.arr(
      Json.obj(
        "title" -> "testSection",
        "data" -> Json.obj(
          "questionId" -> "questionId",
          "question" -> "question",
          "answer" -> "answer",
          "answerValue" -> "answerValue"
        )
      )
    )
  )

  "saveEligibility" must {
    "return OK with the updated JSON" in new Setup {
      stubSuccessfulLogin()
      stubAudits()
      stubPatch(apiUrl, OK, testJson.toString(), testJson.toString())

      val res = await(connector.saveEligibility(testRegId, testJson))

      res mustBe testJson
    }
  }

}
