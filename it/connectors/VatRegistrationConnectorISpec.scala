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
