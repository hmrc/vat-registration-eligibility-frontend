package controllers

import helpers.IntegrationSpecBase
import identifiers.VATRegistrationExceptionId
import org.jsoup.Jsoup
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class VATRegistrationExceptionISpec extends IntegrationSpecBase {

  val pageUrl = "/registration-exception"
  val yesRadio = "value"
  val noRadio = "value-no"

  "GET /registration-exception" when {
    "an answer exists for the page" must {
      "return OK with the answer pre-populated" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        cacheSessionData(sessionId, VATRegistrationExceptionId, true)

        val res = await(buildClient(pageUrl).get)
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.radioIsSelected(yesRadio) mustBe true
        doc.radioIsSelected(noRadio) mustBe false
      }
    }
    "an answer doesn't exist for the page" must {
      "return OK with an empty form " in new Setup {
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

  s"POST /registration-exception" when {
    "the user answers" must {
      def validateFlow(formValue: String) = {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient("/registration-exception").post(Map("value" -> Seq(formValue))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.MtdInformationController.onPageLoad.url)
      }

      s"redirect to ${controllers.routes.MtdInformationController.onPageLoad} if answer is yes" in new Setup {
        validateFlow("true")
      }

      s"redirect to ${controllers.routes.MtdInformationController.onPageLoad} if answer is no" in new Setup {
        validateFlow("false")
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
