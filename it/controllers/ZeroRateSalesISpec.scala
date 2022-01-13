package controllers

import helpers.{AuthHelper, IntegrationSpecBase, SessionStub}
import identifiers.{RegisteringBusinessId, ThresholdInTwelveMonthsId, ThresholdNextThirtyDaysId, ZeroRatedSalesId}
import models.ConditionalDateFormElement
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Format._
import play.mvc.Http.HeaderNames

import java.time.LocalDate

class ZeroRateSalesISpec extends IntegrationSpecBase with AuthHelper with SessionStub {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build()

  val localDate = LocalDate.of(2020, 1, 1)

  class Setup extends SessionTest(app)

  val internalId = "testInternalId"
  s"POST ${controllers.routes.ZeroRatedSalesController.onSubmit().url}" should {
    s"navigate to ${controllers.routes.VoluntaryInformationController.onPageLoad} when false and in the voluntary flow" in new Setup {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      cacheSessionData(sessionId, ThresholdInTwelveMonthsId.toString, ConditionalDateFormElement(false, None))
      cacheSessionData(sessionId, ThresholdNextThirtyDaysId.toString, ConditionalDateFormElement(false, None))

      val request = buildClient(controllers.routes.ZeroRatedSalesController.onSubmit().url)
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("false")))

      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VoluntaryInformationController.onPageLoad.url)
      verifySessionCacheData(sessionId, ZeroRatedSalesId.toString, Option.apply[Boolean](false))
    }
    s"navigate to ${controllers.routes.VoluntaryInformationController.onPageLoad} when true and in the voluntary flow" in new Setup {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      cacheSessionData(sessionId, ThresholdInTwelveMonthsId.toString, ConditionalDateFormElement(false, None))
      cacheSessionData(sessionId, ThresholdNextThirtyDaysId.toString, ConditionalDateFormElement(false, None))

      val request = buildClient(controllers.routes.ZeroRatedSalesController.onSubmit().url)
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("true")))

      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VoluntaryInformationController.onPageLoad.url)
      verifySessionCacheData(sessionId, ZeroRatedSalesId.toString, Option.apply[Boolean](true))
    }
    "navigate to VAT Exemption when true and in the mandatory flow" in new Setup {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      cacheSessionData(sessionId, ThresholdInTwelveMonthsId.toString, ConditionalDateFormElement(true, Some(localDate)))

      val request = buildClient(controllers.routes.ZeroRatedSalesController.onSubmit().url)
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("true")))

      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VATExemptionController.onPageLoad.url)
      verifySessionCacheData(sessionId, ZeroRatedSalesId.toString, Option.apply[Boolean](true))
    }

    s"navigate to ${controllers.routes.MandatoryInformationController.onPageLoad} when false and in the mandatory flow" in new Setup {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      cacheSessionData(sessionId, ThresholdInTwelveMonthsId.toString, ConditionalDateFormElement(true, Some(localDate)))

      val request = buildClient(controllers.routes.ZeroRatedSalesController.onSubmit().url)
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("false")))

      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.MandatoryInformationController.onPageLoad.url)
      verifySessionCacheData(sessionId, ZeroRatedSalesId.toString, Option.apply[Boolean](false))
      verifySessionCacheData(sessionId, RegisteringBusinessId.toString, Option.empty[Boolean])
    }
  }
}