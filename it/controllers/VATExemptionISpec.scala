package controllers

import config.FrontendAppConfig
import featureswitch.core.config.ExceptionExemptionFlow
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

  val config: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  class Setup extends SessionTest(app)

  s"${controllers.routes.VATExemptionController.onSubmit()}" when {
    "ExceptionExemption flow feature switch is not enabled" should {
      s"redirect the user to ${config.otrsUrl} when answer is true and MTD is mandatory" in new Setup {
        stubSuccessfulLogin()
        stubSuccessfulRegIdGet()
        stubAudits()

        cacheSessionData[ConditionalDateFormElement](sessionId, s"$ThresholdInTwelveMonthsId", ConditionalDateFormElement(true, None))
        cacheSessionData[ConditionalDateFormElement](sessionId, s"$ThresholdNextThirtyDaysId", ConditionalDateFormElement(false, None))

        val request = buildClient("/vat-exemption").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
          .post(Map(
            "value" -> Seq("true")
          ))
        val response = await(request)
        response.status mustBe 303
        response.header(HeaderNames.LOCATION) mustBe Some(("/check-if-you-can-register-for-vat/cant-register/OTRS"))
      }
      s"redirect the user to ${controllers.routes.MandatoryInformationController.onPageLoad} when answer is false and MTD is mandatory" in new Setup {
        stubSuccessfulLogin()
        stubSuccessfulRegIdGet()
        stubAudits()

        cacheSessionData[ConditionalDateFormElement](sessionId, s"$ThresholdInTwelveMonthsId", ConditionalDateFormElement(true, None))
        cacheSessionData[ConditionalDateFormElement](sessionId, s"$ThresholdNextThirtyDaysId", ConditionalDateFormElement(false, None))

        val request = buildClient("/vat-exemption").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
          .post(Map(
            "value" -> Seq("false")
          ))
        val response = await(request)
        response.status mustBe 303
        response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.MandatoryInformationController.onPageLoad.url)
      }
    }
    "ExceptionExemption flow feature switch is enabled" should {
      s"redirect the user to ${controllers.routes.MandatoryInformationController.onPageLoad} when answer is true and MTD is mandatory" in new Setup {
        enable(ExceptionExemptionFlow)
        stubSuccessfulLogin()
        stubSuccessfulRegIdGet()
        stubAudits()

        cacheSessionData[ConditionalDateFormElement](sessionId, s"$ThresholdInTwelveMonthsId", ConditionalDateFormElement(true, None))
        cacheSessionData[ConditionalDateFormElement](sessionId, s"$ThresholdNextThirtyDaysId", ConditionalDateFormElement(false, None))

        val request = buildClient("/vat-exemption").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
          .post(Map(
            "value" -> Seq("true")
          ))
        val response = await(request)
        response.status mustBe 303
        response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.MandatoryInformationController.onPageLoad.url)
      }
      s"redirect the user to ${controllers.routes.MandatoryInformationController.onPageLoad} when answer is false and MTD is mandatory" in new Setup {
        enable(ExceptionExemptionFlow)
        stubSuccessfulLogin()
        stubSuccessfulRegIdGet()
        stubAudits()

        cacheSessionData[ConditionalDateFormElement](sessionId, s"$ThresholdInTwelveMonthsId", ConditionalDateFormElement(true, None))
        cacheSessionData[ConditionalDateFormElement](sessionId, s"$ThresholdNextThirtyDaysId", ConditionalDateFormElement(false, None))

        val request = buildClient("/vat-exemption").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
          .post(Map(
            "value" -> Seq("false")
          ))
        val response = await(request)
        response.status mustBe 303
        response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.MandatoryInformationController.onPageLoad.url)
      }
    }

  }

}
