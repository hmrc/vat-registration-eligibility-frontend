package controllers

import com.github.tomakehurst.wiremock.client.WireMock._
import featureswitch.core.config.{FeatureSwitching, TrafficManagement}
import helpers._
import identifiers.BusinessEntityId
import models._
import play.api.Application
import play.api.http.Status.NOT_FOUND
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, Json}
import play.api.test.Helpers.{CREATED, OK, TOO_MANY_REQUESTS}
import play.mvc.Http.HeaderNames
import services.TrafficManagementService

import java.time.LocalDate

class TaxableSuppliesInUkControllerISpec extends IntegrationSpecBase
  with AuthHelper
  with SessionStub
  with FeatureSwitching
  with TrafficManagementStub
  with S4LStub {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build()

  val testDate: LocalDate = LocalDate.now

  val testEnrolments: JsArray = Json.arr(Json.obj(
    "key" -> TrafficManagementService.selfAssesmentEnrolment,
    "identifiers" -> Json.arr(
      Json.obj(
        "key" -> "testKey",
        "value" -> "testValue"
      )
    )
  ))

  val pageUrl: String = routes.TaxableSuppliesInUkController.onSubmit.toString

  s"POST $pageUrl" should {
    "redirect to Do Not Need To Register if the answer is no" in {
      disable(TrafficManagement)
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)

      val request = buildClient(pageUrl).withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map(
          "value" -> Seq("false"))
        )
      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.DoNotNeedToRegisterController.onPageLoad.url)
    }

    "redirect to the Threshold Taxable Supplies page if the feature switch is disabled" in {
      disable(TrafficManagement)
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubUpsertRegistrationInformation(RegistrationInformation(testInternalId, testRegId, Draft, Some(LocalDate.now), VatReg))
      stubAudits()
      stubS4LGetNothing(testRegId)

      val request = buildClient(pageUrl).withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map(
          "value" -> Seq("true"))
        )
      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdTaxableSuppliesController.onPageLoad.url)
    }

    "redirect to the Threshold Taxable Supplies page if the answer is yes and TrafficManagement returns allocated" in {
      enable(TrafficManagement)
      stubSuccessfulLogin(enrolments = testEnrolments)
      stubSuccessfulRegIdGet()
      stubAudits()
      stubAllocation(testRegId)(CREATED)
      stubFor(
        put(urlMatching("/save4later/vat-registration-eligibility-frontend/testRegId/data/eligibility-data"))
          .willReturn(aResponse.withStatus(CREATED).withBody(Json.stringify(Json.obj("id" -> testRegId, "data" -> Json.obj()))))
      )
      stubGetRegistrationInformation(NOT_FOUND, None)
      stubS4LGetNothing(testRegId)

      cacheSessionData[BusinessEntity](testInternalId, s"$BusinessEntityId", UKCompany)

      val request = buildClient(pageUrl).withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map(
          "value" -> Seq("true"))
        )
      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdTaxableSuppliesController.onPageLoad.url)
    }

    "redirect to OTRS if the answer is yes and TrafficManagement returns Quota Reached" in {
      enable(TrafficManagement)
      stubSuccessfulLogin(enrolments = testEnrolments)
      stubSuccessfulRegIdGet()
      stubAudits()
      stubAllocation(testRegId)(TOO_MANY_REQUESTS)
      stubGetRegistrationInformation(NOT_FOUND, None)
      stubS4LGetNothing(testRegId)

      cacheSessionData[BusinessEntity](testInternalId, s"$BusinessEntityId", UKCompany)

      val request = buildClient(pageUrl).withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map(
          "value" -> Seq("true"))
        )
      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some("https://tax.service.gov.uk/business-registration/select-taxes")
    }

    "redirect to the Threshold Taxable Supplies page if the answer is yes, TrafficManagement returns Quota Reached but RegistrationInformation matches" in {
      enable(TrafficManagement)
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubAllocation(testRegId)(TOO_MANY_REQUESTS)
      stubGetRegistrationInformation(OK, Some(RegistrationInformation(testInternalId, testRegId, Draft, Some(testDate), VatReg)))
      stubS4LGetNothing(testRegId)

      val request = buildClient(pageUrl).withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map(
          "value" -> Seq("true"))
        )
      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdTaxableSuppliesController.onPageLoad.url)
    }
  }
}