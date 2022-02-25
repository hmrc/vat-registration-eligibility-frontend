package controllers

import helpers.IntegrationSpecBase
import identifiers.{ThresholdInTwelveMonthsId, TurnoverEstimateId}
import models.{ConditionalDateFormElement, TurnoverEstimateFormElement}
import org.jsoup.Jsoup
import play.api.test.Helpers._
import play.api.http.HeaderNames
import play.api.libs.json.Json

import java.time.LocalDate

class TurnoverEstimateControllerISpec extends IntegrationSpecBase {

  val pageUrl = "/vat-taxable-turnover"
  val turnoverBox = "turnoverEstimateAmount"
  val testDate = LocalDate.of(2022, 2, 23)

  "GET /vat-taxable-turnover" when {
    "an answer exists for the page" must {
      "return OK with the answer pre-populated" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        val testTurnoverEstimate = "1"

        cacheSessionData(sessionId, TurnoverEstimateId, TurnoverEstimateFormElement(testTurnoverEstimate))

        val res = await(buildClient(pageUrl).get)
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.textboxContainsValue(turnoverBox, testTurnoverEstimate) mustBe true
      }
    }
    "an answer doesn't exist for the page" must {
      "return OK with an empty form" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        val nothing = ""

        val res = await(buildClient(pageUrl).get)
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.textboxContainsValue(turnoverBox, nothing) mustBe true
      }
    }
  }

  "POST /vat-taxable-turnover" when {
    "the user enters a valid, non-zero value" must {
      "redirect to the Zero Rated Sales page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Json.obj(turnoverBox -> "1")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.ZeroRatedSalesController.onPageLoad.url)
      }
    }
    "the user enters zero" must {
      "redirect to the Voluntary MTD Information page when voluntary" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Json.obj(turnoverBox -> "0")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.VoluntaryInformationController.onPageLoad.url)
      }
      "redirect to the Mandatory MTD Information page when mandatory" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        cacheSessionData(sessionId, ThresholdInTwelveMonthsId, ConditionalDateFormElement(true, Some(testDate)))

        val res = await(buildClient(pageUrl).post(Json.obj(turnoverBox -> "0")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.MandatoryInformationController.onPageLoad.url)
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
