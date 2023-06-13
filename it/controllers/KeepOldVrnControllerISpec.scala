package controllers


import helpers.{IntegrationSpecBase, S4LStub}
import identifiers.{KeepOldVrnId, TermsAndConditionsId}
import org.jsoup.Jsoup
import play.api.http.Status._
import play.mvc.Http.HeaderNames

class KeepOldVrnControllerISpec extends IntegrationSpecBase with S4LStub {

  val pageUrl = "/keep-old-vrn"
  val yesRadio = "value"
  val noRadio = "value-no"

  "GET /keep-old-vrn" when {
    "an answer exists for the page" must {
      "return OK with the answer pre-populated" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        cacheSessionData(sessionIdStr, KeepOldVrnId, true)

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

  s"POST /keep-old-vrn" when {
    "the user answers" must {
      "redirect to Mtd information page and clear down old T&C answer if the answer is no" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        cacheSessionData[Boolean](sessionIdStr, TermsAndConditionsId, true)

        val res = await(buildClient(pageUrl).post(Map("value" -> Seq("false"))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.MtdInformationController.onPageLoad.url)
        verifySessionCacheData[Boolean](sessionIdStr, TermsAndConditionsId, None)
      }

      "redirect to Terms & Conditions page if the answer is yes" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        val res = await(buildClient("/keep-old-vrn").post(Map("value" -> Seq("true"))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TermsAndConditionsController.onPageLoad.url)
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