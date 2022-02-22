package controllers

import featureswitch.core.config.NETPFlow
import helpers.IntegrationSpecBase
import identifiers.FixedEstablishmentId
import org.jsoup.Jsoup
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class FixedEstablishmentControllerISpec extends IntegrationSpecBase {

  val pageUrl = "/fixed-establishment"
  val yesRadio = "value"
  val noRadio = "value-no"

  "GET /fixed-establishment" when {
    "an answer exists for the page" must {
      "return OK with the answer pre=populated" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        cacheSessionData(sessionId, FixedEstablishmentId, true)

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

  "POST /fixed-establishment" when {
    "the user answers 'Yes'" must {
      "redirect to the Business Entity page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Json.obj("value" -> "true")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessEntityController.onPageLoad.url)
      }
    }
    "the user answers 'No'" when {
      "if the NETPFlow FS is enabled" must {
        "redirect to the Business Entity Overseas page" in new Setup {
          enable(NETPFlow)
          stubSuccessfulLogin()
          stubAudits()

          val res = await(buildClient(pageUrl).post(Json.obj("value" -> "false")))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessEntityOverseasController.onPageLoad.url)
        }
      }
      "redirect to the Eligibility Dropout page" in new Setup {
        disable(NETPFlow)
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Json.obj("value" -> "false")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.EligibilityDropoutController.internationalActivitiesDropout.url)
      }
    }
    "the user doesn't answer" must {
      "return BAS_REQUEST" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Json.obj()))

        res.status mustBe BAD_REQUEST
      }
    }
  }

}
