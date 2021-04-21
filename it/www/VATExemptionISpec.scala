package www

import helpers.{AuthHelper, IntegrationSpecBase, SessionStub}
import identifiers.{ThresholdInTwelveMonthsId, ThresholdNextThirtyDaysId}
import models.ConditionalDateFormElement
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.mvc.Http.HeaderNames

class VATExemptionISpec extends IntegrationSpecBase with AuthHelper with SessionStub {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build()

  s"${controllers.routes.VATExemptionController.onSubmit()}" should {
    s"redirect the user to ${controllers.routes.VoluntaryInformationController.onPageLoad()} page when answer is true and MTD is voluntary" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()

      val request = buildClient("/vat-exemption").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map(
          "value" -> Seq("true")
        ))
      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VoluntaryInformationController.onPageLoad().url)

    }
    s"redirect the user to ${controllers.routes.VoluntaryInformationController.onPageLoad()} when answer is false and MTD is voluntary" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()

      val request = buildClient("/vat-exemption").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map(
          "value" -> Seq("false")
        ))
      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VoluntaryInformationController.onPageLoad().url)

    }
    s"redirect the user to ${controllers.routes.MandatoryInformationController.onPageLoad()} when answer is true and MTD is mandatory" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()

      cacheSessionData[ConditionalDateFormElement]("testInternalId", s"$ThresholdInTwelveMonthsId", ConditionalDateFormElement(true, None))
      cacheSessionData[ConditionalDateFormElement]("testInternalId", s"$ThresholdNextThirtyDaysId", ConditionalDateFormElement(false, None))

      val request = buildClient("/vat-exemption").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map(
          "value" -> Seq("true")
        ))
      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.MandatoryInformationController.onPageLoad().url)
    }
    s"redirect the user to ${controllers.routes.MandatoryInformationController.onPageLoad()} when answer is false and MTD is mandatory" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()

      cacheSessionData[ConditionalDateFormElement]("testInternalId", s"$ThresholdInTwelveMonthsId", ConditionalDateFormElement(true, None))
      cacheSessionData[ConditionalDateFormElement]("testInternalId", s"$ThresholdNextThirtyDaysId", ConditionalDateFormElement(false, None))

      val request = buildClient("/vat-exemption").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map(
          "value" -> Seq("false")
        ))
      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.MandatoryInformationController.onPageLoad().url)
    }
  }

}
