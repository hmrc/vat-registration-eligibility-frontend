package controllers

import com.github.tomakehurst.wiremock.client.WireMock._
import featureswitch.core.config.{FeatureSwitching, TrafficManagement}
import helpers._
import identifiers.{BusinessEntityId, FixedEstablishmentId, RegistrationReasonId}
import models._
import play.api.http.Status.{NOT_FOUND, SEE_OTHER}
import play.api.libs.json.{JsArray, Json}
import play.api.test.Helpers.{CREATED, OK, TOO_MANY_REQUESTS}
import play.mvc.Http.HeaderNames
import services.TrafficManagementService

import java.time.LocalDate

class TrafficManagementResolverControllerISpec extends IntegrationSpecBase
  with FeatureSwitching
  with TrafficManagementStub
  with S4LStub {

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
    "the user has a fixed establishment in UK" must {
      "redirect to the Threshold In Twelve Months page if the feature switch is disabled" in new Setup {
        disable(TrafficManagement)
        stubSuccessfulLogin()
        stubUpsertRegistrationInformation(testRegId)(RegistrationInformation(testInternalId, testRegId, Draft, Some(LocalDate.now), VatReg))
        stubAudits()
        stubS4LGetNothing(testRegId)

        cacheSessionData[Boolean](sessionId, FixedEstablishmentId, true)

        val res = await(buildClient(pageUrl).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdInTwelveMonthsController.onPageLoad.url)
      }

      "redirect to the Threshold In Twelve Months page if TrafficManagement returns allocated" in new Setup {
        enable(TrafficManagement)
        stubSuccessfulLogin(enrolments = testEnrolments)
        stubAudits()
        stubAllocation(testRegId)(CREATED)
        stubFor(
          put(urlMatching("/save4later/vat-registration-eligibility-frontend/testRegId/data/eligibility-data"))
            .willReturn(aResponse.withStatus(CREATED).withBody(Json.stringify(Json.obj("id" -> testRegId, "data" -> Json.obj()))))
        )
        stubGetRegistrationInformation(testRegId)(NOT_FOUND, None)
        stubS4LGetNothing(testRegId)

        cacheSessionData[BusinessEntity](sessionId, BusinessEntityId, UKCompany)
        cacheSessionData[Boolean](sessionId, FixedEstablishmentId, true)
        cacheSessionData[RegistrationReason](sessionId, RegistrationReasonId, SellingGoodsAndServices)

        val res = await(buildClient(pageUrl).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdInTwelveMonthsController.onPageLoad.url)
      }

      "redirect to the DateOfBusinessTransfer page if TrafficManagement returns allocated for COLE reg reason" in new Setup {
        enable(TrafficManagement)
        stubSuccessfulLogin(enrolments = testEnrolments)
        stubAudits()
        stubAllocation(testRegId)(CREATED)
        stubFor(
          put(urlMatching("/save4later/vat-registration-eligibility-frontend/testRegId/data/eligibility-data"))
            .willReturn(aResponse.withStatus(CREATED).withBody(Json.stringify(Json.obj("id" -> testRegId, "data" -> Json.obj()))))
        )
        stubGetRegistrationInformation(testRegId)(NOT_FOUND, None)
        stubS4LGetNothing(testRegId)

        cacheSessionData[BusinessEntity](sessionId, BusinessEntityId, UKCompany)
        cacheSessionData[Boolean](sessionId, FixedEstablishmentId, true)
        cacheSessionData[RegistrationReason](sessionId, RegistrationReasonId, ChangingLegalEntityOfBusiness)

        val res = await(buildClient(pageUrl).get())

       res.status mustBe SEE_OTHER
       res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.DateOfBusinessTransferController.onPageLoad.url)
      }

      "redirect to the Turnover Estimate page if TrafficManagement returns Allocated for an Established UK Exporter reg reason" in new Setup {
        enable(TrafficManagement)
        stubSuccessfulLogin()
        stubAudits()
        stubAllocation(testRegId)(CREATED)
        stubFor(
          put(urlMatching("/save4later/vat-registration-eligibility-frontend/testRegId/data/eligibility-data"))
            .willReturn(aResponse.withStatus(CREATED).withBody(Json.stringify(Json.obj("id" -> testRegId, "data" -> Json.obj()))))
        )
        stubGetRegistrationInformation(testRegId)(NOT_FOUND, None)
        stubS4LGetNothing(testRegId)

        cacheSessionData[BusinessEntity](sessionId, BusinessEntityId, UKCompany)
        cacheSessionData[Boolean](sessionId, FixedEstablishmentId, true)
        cacheSessionData[RegistrationReason](sessionId, RegistrationReasonId, UkEstablishedOverseasExporter)

        val res = await(buildClient(pageUrl).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TurnoverEstimateController.onPageLoad.url)
      }

      "redirect to VAT Exception Page if TrafficManagement returns Quota Reached" in new Setup {
        enable(TrafficManagement)
        stubSuccessfulLogin(enrolments = testEnrolments)
        stubAudits()
        stubAllocation(testRegId)(TOO_MANY_REQUESTS)
        stubGetRegistrationInformation(testRegId)(NOT_FOUND, None)
        stubS4LGetNothing(testRegId)

        cacheSessionData[BusinessEntity](sessionId, BusinessEntityId, UKCompany)
        cacheSessionData[Boolean](sessionId, FixedEstablishmentId, true)

        val res = await(buildClient(pageUrl).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VATExceptionKickoutController.onPageLoad.url)
      }

      "redirect to the Threshold In Twelve Months page if TrafficManagement returns Quota Reached but RegistrationInformation matches" in new Setup {
        enable(TrafficManagement)
        stubSuccessfulLogin()
        stubAudits()
        stubAllocation(testRegId)(TOO_MANY_REQUESTS)
        stubGetRegistrationInformation(testRegId)(OK, Some(RegistrationInformation(testInternalId, testRegId, Draft, Some(testDate), VatReg)))
        stubS4LGetNothing(testRegId)

        cacheSessionData[Boolean](sessionId, FixedEstablishmentId, true)

        val res = await(buildClient(pageUrl).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdInTwelveMonthsController.onPageLoad.url)
      }
    }

    "the user doesn't have a fixed establishment in UK" must {
      "redirect to the Threshold Taxable Supplies page if the feature switch is disabled" in new Setup {
        disable(TrafficManagement)
        stubSuccessfulLogin()
        stubUpsertRegistrationInformation(testRegId)(RegistrationInformation(testInternalId, testRegId, Draft, Some(LocalDate.now), VatReg))
        stubAudits()
        stubS4LGetNothing(testRegId)

        cacheSessionData[Boolean](sessionId, FixedEstablishmentId, false)

        val res = await(buildClient(pageUrl).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdTaxableSuppliesController.onPageLoad.url)
      }

      "redirect to the Threshold Taxable Supplies page if TrafficManagement returns allocated" in new Setup {
        enable(TrafficManagement)
        stubSuccessfulLogin(enrolments = testEnrolments)
        stubAudits()
        stubAllocation(testRegId)(CREATED)
        stubFor(
          put(urlMatching("/save4later/vat-registration-eligibility-frontend/testRegId/data/eligibility-data"))
            .willReturn(aResponse.withStatus(CREATED).withBody(Json.stringify(Json.obj("id" -> testRegId, "data" -> Json.obj()))))
        )
        stubGetRegistrationInformation(testRegId)(NOT_FOUND, None)
        stubS4LGetNothing(testRegId)

        cacheSessionData[BusinessEntity](sessionId, BusinessEntityId, Overseas)
        cacheSessionData[Boolean](sessionId, FixedEstablishmentId, false)

        val res = await(buildClient(pageUrl).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdTaxableSuppliesController.onPageLoad.url)
      }

      "redirect to the Date Of Business Transfer page if TrafficManagement returns allocated for a TOGC reg reason" in new Setup {
        enable(TrafficManagement)
        stubSuccessfulLogin(enrolments = testEnrolments)
        stubAudits()
        stubAllocation(testRegId)(CREATED)
        stubFor(
          put(urlMatching("/save4later/vat-registration-eligibility-frontend/testRegId/data/eligibility-data"))
            .willReturn(aResponse.withStatus(CREATED).withBody(Json.stringify(Json.obj("id" -> testRegId, "data" -> Json.obj()))))
        )
        stubGetRegistrationInformation(testRegId)(NOT_FOUND, None)
        stubS4LGetNothing(testRegId)

        cacheSessionData[BusinessEntity](sessionId, BusinessEntityId, Overseas)
        cacheSessionData[RegistrationReason](sessionId, RegistrationReasonId, TakingOverBusiness)
        cacheSessionData[Boolean](sessionId, FixedEstablishmentId, false)

        val res = await(buildClient(pageUrl).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.DateOfBusinessTransferController.onPageLoad.url)
      }

      "redirect to OTRS if TrafficManagement returns Quota Reached" in new Setup {
        enable(TrafficManagement)
        stubSuccessfulLogin(enrolments = testEnrolments)
        stubAudits()
        stubAllocation(testRegId)(TOO_MANY_REQUESTS)
        stubGetRegistrationInformation(testRegId)(NOT_FOUND, None)
        stubS4LGetNothing(testRegId)

        cacheSessionData[BusinessEntity](sessionId, BusinessEntityId, Overseas)
        cacheSessionData[Boolean](sessionId, FixedEstablishmentId, false)

        val res = await(buildClient(pageUrl).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some("https://tax.service.gov.uk/business-registration/select-taxes")
      }

      "redirect to the Threshold Taxable Supplies page if TrafficManagement returns Quota Reached but RegistrationInformation matches" in new Setup {
        enable(TrafficManagement)
        stubSuccessfulLogin()
        stubAudits()
        stubAllocation(testRegId)(TOO_MANY_REQUESTS)
        stubGetRegistrationInformation(testRegId)(OK, Some(RegistrationInformation(testInternalId, testRegId, Draft, Some(testDate), VatReg)))
        stubS4LGetNothing(testRegId)

        cacheSessionData[Boolean](sessionId, FixedEstablishmentId, false)

        val res = await(buildClient(pageUrl).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdTaxableSuppliesController.onPageLoad.url)
      }
    }
  }
}