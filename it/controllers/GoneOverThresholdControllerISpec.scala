

package controllers

import helpers.IntegrationSpecBase
import identifiers.GoneOverThresholdId
import org.jsoup.Jsoup
import play.api.http.Status._
import play.api.libs.json.Json
import play.mvc.Http.HeaderNames

class GoneOverThresholdControllerISpec extends IntegrationSpecBase {

  val pageUrl = "/gone-over-threshold-netp"
  val yesRadio = "value"
  val noRadio = "value-no"

  "GET /gone-over-threshold-netp" when {
    "an answer exists for the page" must {
      "return OK with the answer pre-populated" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        cacheSessionData(sessionId, GoneOverThresholdId, true)

        val res = await(buildClient(pageUrl).get)
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.radioIsSelected(yesRadio) mustBe true
        doc.radioIsSelected(noRadio) mustBe false
      }
    }
    "an answer doesn't exist for the page" must {
      "return OK with an empty form" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).get)
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.radioIsSelected(yesRadio) mustBe false
        doc.radioIsSelected(noRadio) mustBe false
      }
    }
  }

  s"POST /gone-over-threshold-netp" when {
    "the user answers" must {
      s"redirect to ${controllers.routes.TurnoverEstimateController.onPageLoad} when value is true" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map("value" -> "true")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TurnoverEstimateController.onPageLoad().url)
      }

      s"redirect to ${controllers.routes.TurnoverEstimateController.onPageLoad} when value is false" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map("value" -> "false")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TurnoverEstimateController.onPageLoad().url)
      }
    }
    "the user doesn't answer" must {
      "return BAD_REQUEST" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Json.obj()))

        res.status mustBe BAD_REQUEST
      }
    }
  }
}
