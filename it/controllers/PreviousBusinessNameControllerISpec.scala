package controllers

import featureswitch.core.config.FeatureSwitching
import helpers._
import identifiers.PreviousBusinessNameId
import org.jsoup.Jsoup
import play.api.http.Status._
import play.mvc.Http.HeaderNames

class PreviousBusinessNameControllerISpec extends IntegrationSpecBase with FeatureSwitching with S4LStub {

  val pageUrl = "/previous-business-name"
  val testPreviousBusinessName = "Al Pacino Ltd"
  val textbox = "previousBusinessName"

  s"GET /previous-business-name" when {
    "an answer exists for the page" must {
      "return OK with the answer pre-populated" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        cacheSessionData(sessionId, PreviousBusinessNameId, testPreviousBusinessName)

        val res = await(buildClient(pageUrl).get)
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.textboxContainsValue(textbox, testPreviousBusinessName) mustBe true
      }
    }
    "an answer doesn't exist for the page" must {
      "return OK with an empty form" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        val nothing = ""

        val res = await(buildClient(pageUrl).get)
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.textboxContainsValue(textbox, nothing) mustBe true
      }
    }
  }

  s"POST /previous-business-name" should {
    "redirect to Previous VRN" in new Setup {
      stubSuccessfulLogin()
      stubAudits()
      stubS4LGetNothing(testRegId)

      val res = await(buildClient(pageUrl).post(Map(textbox -> testPreviousBusinessName)))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VATNumberController.onPageLoad.url)
    }
    "return a BAD_REQUEST with form errors" in new Setup {
      stubSuccessfulLogin()
      stubAudits()
      stubS4LGetNothing(testRegId)

      val res = await(buildClient(pageUrl).post(Map(textbox -> "")))

      res.status mustBe BAD_REQUEST
    }
  }
}