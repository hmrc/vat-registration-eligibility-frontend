package controllers

import helpers.IntegrationSpecBase
import identifiers.VoluntaryRegistrationId
import org.jsoup.Jsoup
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class VoluntaryRegistrationControllerISpec extends IntegrationSpecBase {

  val pageUrl = "/register-voluntarily"
  val yesRadio = "value"
  val noRadio = "value-no"

  "GET /register-voluntarily" when {
    "an answer already exists for the page" must {
      "return OK with the answer pre-populated" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        cacheSessionData(sessionIdStr, VoluntaryRegistrationId, true)

        val res = await(buildClient(pageUrl).get())
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

        val res = await(buildClient(pageUrl).get())
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.radioIsSelected(yesRadio) mustBe false
        doc.radioIsSelected(noRadio) mustBe false
      }
    }
  }

  "POST /register-voluntarily" when {
    "the user answers 'Yes'" must {
      "redirect to the Mtd information page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map("value" -> "true")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.MtdInformationController.onPageLoad.url)
      }
    }
    "the user answers 'No'" must {
      "redirect to the Chose Not To Register page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map("value" -> "false")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.ChoseNotToRegisterController.onPageLoad.url)
      }
    }
    "the user doesn't answer" must {
      "return BAD_REQUEST" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map[String, String]()))

        res.status mustBe BAD_REQUEST
      }
    }
  }

}
