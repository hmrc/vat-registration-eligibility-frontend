package controllers

import helpers.{AuthHelper, IntegrationSpecBase, SessionStub}
import models.RegistrationReason._
import play.api.Application
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.inject.guice.GuiceApplicationBuilder
import play.mvc.Http.HeaderNames

class RegistrationReasonControllerISpec extends IntegrationSpecBase with AuthHelper with SessionStub {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build()

  "GET /registration-reason" must {
    "return OK" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()

      val res = await(buildClient(controllers.routes.RegistrationReasonController.onPageLoad.url).get)

      res.status mustBe OK
    }
  }

  "POST /registration-reason" must {
    s"redirect to Nino when sellingGoodsAndServices value is selected" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()

      val request = buildClient("/registration-reason").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> sellingGoodsAndServicesKey))

      val response = await(request)
      response.status mustBe SEE_OTHER
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.NinoController.onPageLoad.url)
    }

    s"redirect to Nino when ukEstablishedOverseasExporter value is selected" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()

      val request = buildClient("/registration-reason").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> ukEstablishedOverseasExporterKey))

      val response = await(request)
      response.status mustBe SEE_OTHER
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.NinoController.onPageLoad.url)
    }
  }
}
