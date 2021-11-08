package controllers

import featureswitch.core.config.{FeatureSwitching, IndividualFlow}
import helpers.{AuthHelper, IntegrationSpecBase, S4LStub, SessionStub}
import models.RegistrationReason._
import play.api.Application
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.inject.guice.GuiceApplicationBuilder
import play.mvc.Http.HeaderNames

class RegistrationReasonControllerISpec extends IntegrationSpecBase with AuthHelper with SessionStub with FeatureSwitching with S4LStub {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build()

  "GET /registration-reason" must {
    "return OK" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)

      val res = await(buildClient(controllers.routes.RegistrationReasonController.onPageLoad.url).get)

      res.status mustBe OK
    }
  }

  "POST /registration-reason" must {
    s"redirect to Nino when sellingGoodsAndServices value is selected" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)

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
      stubS4LGetNothing(testRegId)

      val request = buildClient("/registration-reason").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> ukEstablishedOverseasExporterKey))

      val response = await(request)
      response.status mustBe SEE_OTHER
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.NinoController.onPageLoad.url)
    }

    s"redirect to Traffic management resolver when sellingGoodsAndServices value is selected and individual flow enabled" in {
      enable(IndividualFlow)
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)

      val request = buildClient("/registration-reason").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> sellingGoodsAndServicesKey))

      val response = await(request)
      response.status mustBe SEE_OTHER
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TrafficManagementResolverController.resolve.url)
    }

    s"redirect to Traffic management resolver when ukEstablishedOverseasExporter value is selected and individual flow enabled" in {
      enable(IndividualFlow)
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)

      val request = buildClient("/registration-reason").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> ukEstablishedOverseasExporterKey))

      val response = await(request)
      response.status mustBe SEE_OTHER
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TrafficManagementResolverController.resolve.url)
    }
  }
}
