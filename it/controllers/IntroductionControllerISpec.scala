package controllers

import helpers.{AuthHelper, IntegrationSpecBase, SessionStub}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class IntroductionControllerISpec extends IntegrationSpecBase with AuthHelper with SessionStub {

  override implicit lazy val app = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build

  val testUrl = controllers.routes.IntroductionController.onPageLoad.url

  "GET /introduction" must {
    "return OK" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()

      val res = await(buildClient(testUrl).get)

      res.status mustBe OK
    }
  }

  "POST /introduction" must {
    "Redirect to the FixedEstablishmentController" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()

      val res = await(buildClient(testUrl)
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Json.obj()))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.FixedEstablishmentController.onPageLoad().url)
    }
  }

}
