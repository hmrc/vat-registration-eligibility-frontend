package controllers

import helpers.IntegrationSpecBase
import identifiers.{VATExceptionKickoutId, VATRegistrationExceptionId}
import org.jsoup.Jsoup
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames


class VatExceptionKickoutControllerISpec extends IntegrationSpecBase {

  val pageUrl = "/vat-exception-registration"
  val yesRadio = "value"
  val noRadio = "value-no"

  "GET /vat-exception-registration" when {
    "an answer exists for the page" must {
      "return OK with the answer pre-populated" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        cacheSessionData(sessionId, VATExceptionKickoutId, true)

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

  "POST /vat-exception-registration" when {
    "the user answers 'Yes'" must {
      "redirect to the Eligibility Dropout (VATExceptionKickoutId) page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map("value" -> "true")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.EligibilityDropoutController.onPageLoad(VATExceptionKickoutId.toString).url)
      }
    }
    "the user answers 'No'" must {
      "redirect to the Eligibility Dropout (VATExceptionKickoutId) page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map("value" -> "false")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.EligibilityDropoutController.onPageLoad(VATRegistrationExceptionId.toString).url)
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
