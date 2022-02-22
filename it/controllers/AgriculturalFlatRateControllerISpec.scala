package controllers

import helpers.IntegrationSpecBase
import identifiers.AgriculturalFlatRateSchemeId
import org.jsoup.Jsoup
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class AgriculturalFlatRateControllerISpec extends IntegrationSpecBase {

  val pageUrl: String = "/agricultural-flat-rate"

  val yesRadio = "value"
  val noRadio = "value-no"

  s"GET /agricultural-flat-rate" when {
    "an answer for the page already exists" must {
      "return OK with the answer pre-populated" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        cacheSessionData[Boolean](sessionId, AgriculturalFlatRateSchemeId, true)

        val res = await(buildClient(pageUrl).get())
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.radioIsSelected(yesRadio) mustBe true
        doc.radioIsSelected(noRadio) mustBe false
      }
    }
    "no answer exists for the page" must {
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

  "POST /agricultural-flat-rate" when {
    "the user selects 'No'" must {
      "redirect to the International Activities page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Json.obj("value" -> "false")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.InternationalActivitiesController.onPageLoad.url)
      }
    }
    "the user selects 'Yes'" must {
      "redirect to the Eligibility Dropout page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Json.obj("value" -> "true")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.EligibilityDropoutController.onPageLoad(AgriculturalFlatRateSchemeId.toString).url)
      }
    }
    "the user didn't select an answer" must {
      "return BAD_REQUEST" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Json.obj()))

        res.status mustBe BAD_REQUEST
      }
    }
  }

}
