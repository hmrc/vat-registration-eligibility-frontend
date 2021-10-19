package controllers

import helpers.{AuthHelper, IntegrationSpecBase, SessionStub}
import models.RegistrationReason.sellingGoodsAndServicesKey
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.inject.guice.GuiceApplicationBuilder
import play.mvc.Http.HeaderNames

class RegistrationReasonControllerISpec extends IntegrationSpecBase with AuthHelper with SessionStub {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build()

  s"${controllers.routes.RegistrationReasonController.onPageLoad}" should {
    "return OK" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()

      val res = await(buildClient(controllers.routes.RegistrationReasonController.onPageLoad.url).get)

      res.status mustBe OK
    }

    s"redirect to ${controllers.routes.RegistrationReasonController.onPageLoad} when value is selected" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()

      val request = buildClient("/registration-reason").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> sellingGoodsAndServicesKey))

      val response = await(request)
      response.status mustBe SEE_OTHER
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.RegistrationReasonController.onPageLoad.url)
    }

    s"redirect to ${controllers.routes.RegistrationReasonController.onPageLoad} when there is no value" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()

      val request = buildClient("/registration-reason").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> ""))

      val response = await(request)
      response.status mustBe BAD_REQUEST
    }
  }
}
