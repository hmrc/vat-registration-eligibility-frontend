package controllers

import helpers.IntegrationSpecBase
import identifiers.{RegisteringBusinessId, ThresholdInTwelveMonthsId, ThresholdNextThirtyDaysId, ZeroRatedSalesId}
import models.ConditionalDateFormElement
import org.jsoup.Jsoup
import play.api.http.Status.SEE_OTHER
import play.api.libs.json.Format._
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

import java.time.LocalDate

class ZeroRateSalesControllerISpec extends IntegrationSpecBase {

  val pageUrl = "/zero-rated-sales"
  val localDate = LocalDate.of(2020, 1, 1)
  val radioField = "value"
  val internalId = "testInternalId"

  "GET /zero-rated-sales" when {
    "an answer exists for the page" must {
      "return OK with the answer pre-populated" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        cacheSessionData(sessionId, ZeroRatedSalesId, true)

        val res = await(buildClient(pageUrl).get)
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.radioIsSelected(radioField) mustBe true
      }
    }
    "an answer doesn't exist for the page" must {
      "return OK with the answer pre-populated" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).get)
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.radioIsSelected(radioField) mustBe false
      }
    }
  }

  s"POST /zero-rated-sales" when {
    "the user answers" must {
      s"navigate to ${controllers.routes.MtdInformationController.onPageLoad} when false and in the voluntary flow" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        cacheSessionData(sessionId, ThresholdInTwelveMonthsId, ConditionalDateFormElement(false, None))
        cacheSessionData(sessionId, ThresholdNextThirtyDaysId, ConditionalDateFormElement(false, None))

        val res = await(buildClient(controllers.routes.ZeroRatedSalesController.onSubmit().url).post(Map("value" -> Seq("false"))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.MtdInformationController.onPageLoad.url)
        verifySessionCacheData(sessionId, ZeroRatedSalesId, Option.apply[Boolean](false))
      }
      s"navigate to ${controllers.routes.MtdInformationController.onPageLoad} when true and in the voluntary flow" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        cacheSessionData(sessionId, ThresholdInTwelveMonthsId, ConditionalDateFormElement(false, None))
        cacheSessionData(sessionId, ThresholdNextThirtyDaysId, ConditionalDateFormElement(false, None))

        val res = await(buildClient(controllers.routes.ZeroRatedSalesController.onSubmit().url)
          .post(Map("value" -> Seq("true"))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.MtdInformationController.onPageLoad.url)
        verifySessionCacheData(sessionId, ZeroRatedSalesId, Option.apply[Boolean](true))
      }
      "navigate to VAT Exemption when true and in the mandatory flow" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        cacheSessionData(sessionId, ThresholdInTwelveMonthsId, ConditionalDateFormElement(true, Some(localDate)))

        val res = await(buildClient(controllers.routes.ZeroRatedSalesController.onSubmit().url)
          .post(Map("value" -> Seq("true"))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VATExemptionController.onPageLoad.url)
        verifySessionCacheData(sessionId, ZeroRatedSalesId, Option.apply[Boolean](true))
      }

      s"navigate to ${controllers.routes.MtdInformationController.onPageLoad} when false and in the mandatory flow" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        cacheSessionData(sessionId, ThresholdInTwelveMonthsId, ConditionalDateFormElement(true, Some(localDate)))

        val res = await(buildClient(controllers.routes.ZeroRatedSalesController.onSubmit().url)
          .post(Map("value" -> Seq("false"))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.MtdInformationController.onPageLoad.url)
        verifySessionCacheData(sessionId, ZeroRatedSalesId, Option.apply[Boolean](false))
        verifySessionCacheData(sessionId, RegisteringBusinessId, Option.empty[Boolean])
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