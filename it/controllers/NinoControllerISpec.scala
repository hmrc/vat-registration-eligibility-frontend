package controllers

import com.github.tomakehurst.wiremock.client.WireMock._
import featureswitch.core.config.{FeatureSwitching, TrafficManagement}
import helpers.{AuthHelper, IntegrationSpecBase, SessionStub, TrafficManagementStub}
import identifiers.{BusinessEntityId, RegistrationReasonId}
import models._
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, Json}
import play.api.test.Helpers.{CREATED, OK, TOO_MANY_REQUESTS}
import play.mvc.Http.HeaderNames
import services.TrafficManagementService

import java.time.LocalDate

class NinoControllerISpec extends IntegrationSpecBase with AuthHelper with SessionStub with FeatureSwitching with TrafficManagementStub {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build()

  val testDate: LocalDate = LocalDate.now

  val testEnrolments: JsArray = Json.arr(Json.obj(
    "key" -> TrafficManagementService.companyEnrolment,
    "identifiers" -> Json.arr(
      Json.obj(
        "key" -> "testKey",
        "value" -> "testValue"
      )
    )
  ))

  s"${controllers.routes.NinoController.onSubmit}" should {
    "redirect to VAT Exception if the answer is no" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()

      val request = buildClient("/have-nino").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map(
          "value" -> Seq("false"))
        )
      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VATExceptionKickoutController.onPageLoad.url)
    }
    "redirect to Threshold In Twelve Months if the feature switch is disabled" in {
      disable(TrafficManagement)
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubUpsertRegistrationInformation(RegistrationInformation(testInternalId, testRegId, Draft, Some(LocalDate.now), VatReg))
      stubAudits()

      val request = buildClient("/have-nino").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map(
          "value" -> Seq("true"))
        )
      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdInTwelveMonthsController.onPageLoad.url)
    }
    "redirect to Threshold In Twelve Months if the answer is yes and TrafficManagement returns allocated" in {
      enable(TrafficManagement)
      stubSuccessfulLogin(enrolments = testEnrolments)
      stubSuccessfulRegIdGet()
      stubAudits()
      stubAllocation(testRegId)(CREATED)
      stubFor(put(urlMatching("/save4later/vat-registration-eligibility-frontend/testRegId/data/eligibility-data"))
        .willReturn(aResponse.withStatus(CREATED).withBody(Json.stringify(Json.obj("id" -> testRegId, "data" -> Json.obj())))))

      cacheSessionData[BusinessEntity](testInternalId, s"$BusinessEntityId", UKCompany)

      val request = buildClient("/have-nino").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map(
          "value" -> Seq("true"))
        )
      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdInTwelveMonthsController.onPageLoad.url)
    }
    "redirect to VAT Exception if the answer is yes and TrafficManagement returns Quota Reached" in {
      enable(TrafficManagement)
      stubSuccessfulLogin(enrolments = testEnrolments)
      stubSuccessfulRegIdGet()
      stubAudits()
      stubAllocation(testRegId)(TOO_MANY_REQUESTS)

      cacheSessionData[BusinessEntity](testInternalId, s"$BusinessEntityId", UKCompany)

      val request = buildClient("/have-nino").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map(
          "value" -> Seq("true"))
        )
      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VATExceptionKickoutController.onPageLoad.url)
    }
    "redirect to Threshold In Twelve Months if the answer is yes, TrafficManagement returns Quota Reached but RegistrationInformation matches" in {
      enable(TrafficManagement)
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubAllocation(testRegId)(TOO_MANY_REQUESTS)
      stubGetRegistrationInformation(OK, Some(RegistrationInformation(testInternalId, testRegId, Draft, Some(testDate), VatReg)))

      val request = buildClient("/have-nino").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map(
          "value" -> Seq("true"))
        )
      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdInTwelveMonthsController.onPageLoad.url)
    }

    "redirect to Turnover Estimate if the answer is yes and UkEstablishedOverseasExporter registration reason is selected" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubGetRegistrationInformation(OK, Some(RegistrationInformation(testInternalId, testRegId, Draft, Some(testDate), VatReg)))

      cacheSessionData[RegistrationReason](testInternalId, s"$RegistrationReasonId", UkEstablishedOverseasExporter)

      val request = buildClient("/have-nino").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map(
          "value" -> Seq("true"))
        )
      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TurnoverEstimateController.onPageLoad.url)
    }
  }
}