package controllers

import com.github.tomakehurst.wiremock.client.WireMock._
import featureswitch.core.config.{FeatureSwitching, TrafficManagement}
import helpers._
import identifiers.{BusinessEntityId, FixedEstablishmentId, RegistrationReasonId}
import models._
import play.api.Application
import play.api.http.Status.NOT_FOUND
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, Json}
import play.api.test.Helpers.{CREATED, OK, TOO_MANY_REQUESTS}
import play.mvc.Http.HeaderNames
import services.TrafficManagementService

import java.time.LocalDate

class TrafficManagementResolverControllerISpec extends IntegrationSpecBase
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

  val pageUrl: String = routes.TrafficManagementResolverController.resolve.toString

  s"POST $pageUrl" when {
    "the user has a fixed establishment in UK" should {
      "redirect to the Threshold In Twelve Months page if the feature switch is disabled" in {
        disable(TrafficManagement)
        stubSuccessfulLogin()
        stubSuccessfulRegIdGet()
        stubUpsertRegistrationInformation(testRegId)(RegistrationInformation(testInternalId, testRegId, Draft, Some(LocalDate.now), VatReg))
        stubAudits()
        stubS4LGetNothing(testRegId)

        cacheSessionData[Boolean](testInternalId, s"$FixedEstablishmentId", true)

        val request = buildClient(pageUrl).get()

        val response = await(request)
        response.status mustBe 303
        response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdInTwelveMonthsController.onPageLoad.url)
      }

      "redirect to the Threshold In Twelve Months page if TrafficManagement returns allocated" in {
        enable(TrafficManagement)
        stubSuccessfulLogin(enrolments = testEnrolments)
        stubSuccessfulRegIdGet()
        stubAudits()
        stubAllocation(testRegId)(CREATED)
        stubFor(
          put(urlMatching("/save4later/vat-registration-eligibility-frontend/testRegId/data/eligibility-data"))
            .willReturn(aResponse.withStatus(CREATED).withBody(Json.stringify(Json.obj("id" -> testRegId, "data" -> Json.obj()))))
        )
        stubGetRegistrationInformation(testRegId)(NOT_FOUND, None)
        stubS4LGetNothing(testRegId)

        cacheSessionData[BusinessEntity](testInternalId, s"$BusinessEntityId", UKCompany)
        cacheSessionData[Boolean](testInternalId, s"$FixedEstablishmentId", true)
        cacheSessionData[RegistrationReason](testInternalId, s"$RegistrationReasonId", SellingGoodsAndServices)

        val request = buildClient(pageUrl).get()

        val response = await(request)
        response.status mustBe 303
        response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdInTwelveMonthsController.onPageLoad.url)
      }

      "redirect to the Turnover Estimate page if TrafficManagement returns Allocated for an Established UK Exporter reg reason" in {
        enable(TrafficManagement)
        stubSuccessfulLogin()
        stubSuccessfulRegIdGet()
        stubAudits()
        stubAllocation(testRegId)(CREATED)
        stubFor(
          put(urlMatching("/save4later/vat-registration-eligibility-frontend/testRegId/data/eligibility-data"))
            .willReturn(aResponse.withStatus(CREATED).withBody(Json.stringify(Json.obj("id" -> testRegId, "data" -> Json.obj()))))
        )
        stubGetRegistrationInformation(testRegId)(NOT_FOUND, None)
        stubS4LGetNothing(testRegId)

        cacheSessionData[BusinessEntity](testInternalId, s"$BusinessEntityId", UKCompany)
        cacheSessionData[Boolean](testInternalId, s"$FixedEstablishmentId", true)
        cacheSessionData[RegistrationReason](testInternalId, s"$RegistrationReasonId", UkEstablishedOverseasExporter)

        val request = buildClient(pageUrl).get()
        val response = await(request)
        response.status mustBe 303
        response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TurnoverEstimateController.onPageLoad.url)
      }

      "redirect to VAT Exception Page if TrafficManagement returns Quota Reached" in {
        enable(TrafficManagement)
        stubSuccessfulLogin(enrolments = testEnrolments)
        stubSuccessfulRegIdGet()
        stubAudits()
        stubAllocation(testRegId)(TOO_MANY_REQUESTS)
        stubGetRegistrationInformation(testRegId)(NOT_FOUND, None)
        stubS4LGetNothing(testRegId)

        cacheSessionData[BusinessEntity](testInternalId, s"$BusinessEntityId", UKCompany)
        cacheSessionData[Boolean](testInternalId, s"$FixedEstablishmentId", true)

        val request = buildClient(pageUrl).get()

        val response = await(request)
        response.status mustBe 303
        response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VATExceptionKickoutController.onPageLoad.url)
      }

      "redirect to the Threshold In Twelve Months page if TrafficManagement returns Quota Reached but RegistrationInformation matches" in {
        enable(TrafficManagement)
        stubSuccessfulLogin()
        stubSuccessfulRegIdGet()
        stubAudits()
        stubAllocation(testRegId)(TOO_MANY_REQUESTS)
        stubGetRegistrationInformation(testRegId)(OK, Some(RegistrationInformation(testInternalId, testRegId, Draft, Some(testDate), VatReg)))
        stubS4LGetNothing(testRegId)

        cacheSessionData[Boolean](testInternalId, s"$FixedEstablishmentId", true)

        val request = buildClient(pageUrl).get()
        val response = await(request)
        response.status mustBe 303
        response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdInTwelveMonthsController.onPageLoad.url)
      }
    }

    "the user doesn't have a fixed establishment in UK" should {
      "redirect to the Threshold Taxable Supplies page if the feature switch is disabled" in {
        disable(TrafficManagement)
        stubSuccessfulLogin()
        stubSuccessfulRegIdGet()
        stubUpsertRegistrationInformation(testRegId)(RegistrationInformation(testInternalId, testRegId, Draft, Some(LocalDate.now), VatReg))
        stubAudits()
        stubS4LGetNothing(testRegId)

        cacheSessionData[Boolean](testInternalId, s"$FixedEstablishmentId", false)

        val request = buildClient(pageUrl).get()

        val response = await(request)
        response.status mustBe 303
        response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdTaxableSuppliesController.onPageLoad.url)
      }

      "redirect to the Threshold Taxable Supplies page if TrafficManagement returns allocated" in {
        enable(TrafficManagement)
        stubSuccessfulLogin(enrolments = testEnrolments)
        stubSuccessfulRegIdGet()
        stubAudits()
        stubAllocation(testRegId)(CREATED)
        stubFor(
          put(urlMatching("/save4later/vat-registration-eligibility-frontend/testRegId/data/eligibility-data"))
            .willReturn(aResponse.withStatus(CREATED).withBody(Json.stringify(Json.obj("id" -> testRegId, "data" -> Json.obj()))))
        )
        stubGetRegistrationInformation(testRegId)(NOT_FOUND, None)
        stubS4LGetNothing(testRegId)

        cacheSessionData[BusinessEntity](testInternalId, s"$BusinessEntityId", Overseas)
        cacheSessionData[Boolean](testInternalId, s"$FixedEstablishmentId", false)

        val request = buildClient(pageUrl).get()

        val response = await(request)
        response.status mustBe 303
        response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdTaxableSuppliesController.onPageLoad.url)
      }

      "redirect to OTRS if TrafficManagement returns Quota Reached" in {
        enable(TrafficManagement)
        stubSuccessfulLogin(enrolments = testEnrolments)
        stubSuccessfulRegIdGet()
        stubAudits()
        stubAllocation(testRegId)(TOO_MANY_REQUESTS)
        stubGetRegistrationInformation(testRegId)(NOT_FOUND, None)
        stubS4LGetNothing(testRegId)

        cacheSessionData[BusinessEntity](testInternalId, s"$BusinessEntityId", Overseas)
        cacheSessionData[Boolean](testInternalId, s"$FixedEstablishmentId", false)

        val request = buildClient(pageUrl).get()

        val response = await(request)
        response.status mustBe 303
        response.header(HeaderNames.LOCATION) mustBe Some("https://tax.service.gov.uk/business-registration/select-taxes")
      }

      "redirect to the Threshold Taxable Supplies page if TrafficManagement returns Quota Reached but RegistrationInformation matches" in {
        enable(TrafficManagement)
        stubSuccessfulLogin()
        stubSuccessfulRegIdGet()
        stubAudits()
        stubAllocation(testRegId)(TOO_MANY_REQUESTS)
        stubGetRegistrationInformation(testRegId)(OK, Some(RegistrationInformation(testInternalId, testRegId, Draft, Some(testDate), VatReg)))
        stubS4LGetNothing(testRegId)

        cacheSessionData[Boolean](testInternalId, s"$FixedEstablishmentId", false)

        val request = buildClient(pageUrl).get()
        val response = await(request)
        response.status mustBe 303
        response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdTaxableSuppliesController.onPageLoad.url)
      }
    }
  }
}