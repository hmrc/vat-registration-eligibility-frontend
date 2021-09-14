

package controllers

import helpers.{AuthHelper, IntegrationSpecBase, SessionStub}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.mvc.Http.HeaderNames

class GoneOverThresholdControllerISpec extends IntegrationSpecBase with AuthHelper with SessionStub {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build()

  s"${controllers.routes.GoneOverThresholdController.onSubmit}" should {
    s"redirect to ${controllers.routes.TurnoverEstimateController.onPageLoad} when value is true" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()

      val request = buildClient("/gone-over-threshold-netp").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> "true"))


      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TurnoverEstimateController.onPageLoad().url)
    }

    s"redirect to ${controllers.routes.TurnoverEstimateController.onPageLoad} when value is false" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()

      val request = buildClient("/gone-over-threshold-netp").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> "false"))


      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TurnoverEstimateController.onPageLoad().url)
    }
  }
}
