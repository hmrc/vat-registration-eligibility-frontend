package controllers

import featureswitch.core.config.{FeatureSwitching, IndividualFlow}
import helpers.{AuthHelper, IntegrationSpecBase, S4LStub, SessionStub}
import identifiers._
import models.RegistrationReason._
import models._
import play.api.Application
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.inject.guice.GuiceApplicationBuilder
import play.mvc.Http.HeaderNames

import java.time.LocalDate

class RegistrationReasonControllerISpec extends IntegrationSpecBase with AuthHelper with SessionStub with FeatureSwitching with S4LStub {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build()

  class Setup extends SessionTest(app)

  "GET /registration-reason" must {
    "return OK" in new Setup {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)

      val res = await(buildClient(controllers.routes.RegistrationReasonController.onPageLoad.url)
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .get)

      res.status mustBe OK
    }
  }

  "POST /registration-reason" must {
    s"redirect to Nino when sellingGoodsAndServices value is selected" in new Setup {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)

      val request = buildClient("/registration-reason")
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> sellingGoodsAndServicesKey))

      val response = await(request)
      response.status mustBe SEE_OTHER
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.NinoController.onPageLoad.url)
    }

    s"redirect to Nino when ukEstablishedOverseasExporter value is selected" in new Setup {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)

      val request = buildClient("/registration-reason")
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> ukEstablishedOverseasExporterKey))

      val response = await(request)
      response.status mustBe SEE_OTHER
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.NinoController.onPageLoad.url)
    }

    s"redirect to Nino when settingUpVatGroup value is selected" in new Setup {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)

      val request = buildClient("/registration-reason")
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> settingUpVatGroupKey))

      val response = await(request)
      response.status mustBe SEE_OTHER
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.NinoController.onPageLoad.url)
    }

    s"redirect to Traffic management resolver when sellingGoodsAndServices value is selected and individual flow enabled" in new Setup {
      enable(IndividualFlow)
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)

      val request = buildClient("/registration-reason")
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> sellingGoodsAndServicesKey))

      val response = await(request)
      response.status mustBe SEE_OTHER
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TrafficManagementResolverController.resolve.url)
    }

    s"redirect to Traffic management resolver when ukEstablishedOverseasExporter value is selected and individual flow enabled" in new Setup {
      enable(IndividualFlow)
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)

      val request = buildClient("/registration-reason")
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> ukEstablishedOverseasExporterKey))

      val response = await(request)
      response.status mustBe SEE_OTHER
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TrafficManagementResolverController.resolve.url)
    }

    s"redirect to Traffic management resolver when settingUpVatGroup value is selected and individual flow enabled" in new Setup {
      enable(IndividualFlow)
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)

      val request = buildClient("/registration-reason")
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> settingUpVatGroupKey))

      val response = await(request)
      response.status mustBe SEE_OTHER
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TrafficManagementResolverController.resolve.url)
    }

    s"redirect to Traffic management resolver for an overseas user selecting takingOverBusiness" in new Setup {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)
      cacheSessionData[BusinessEntity](sessionId, s"$BusinessEntityId", Overseas)

      val request = buildClient("/registration-reason")
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> takingOverBusinessKey))

      val response = await(request)
      response.status mustBe SEE_OTHER
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TrafficManagementResolverController.resolve.url)
    }

    s"redirect to Traffic management resolver for an overseas user selecting changingLegalEntityOfBusiness" in new Setup {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)
      cacheSessionData[BusinessEntity](sessionId, s"$BusinessEntityId", Overseas)

      val request = buildClient("/registration-reason")
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> changingLegalEntityOfBusinessKey))

      val response = await(request)
      response.status mustBe SEE_OTHER
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TrafficManagementResolverController.resolve.url)
    }

    s"redirect to Taxable Supplies Page for an overseas user selecting sellingGoodsAndServices" in new Setup {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)
      cacheSessionData[BusinessEntity](sessionId, s"$BusinessEntityId", Overseas)

      val request = buildClient("/registration-reason")
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> sellingGoodsAndServicesKey))

      val response = await(request)
      response.status mustBe SEE_OTHER
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaxableSuppliesInUkController.onPageLoad.url)
    }

    "clear down threshold/togc data if reg reason is changed" in new Setup {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)
      cacheSessionData[BusinessEntity](sessionId, s"$BusinessEntityId", UKCompany)
      cacheSessionData[RegistrationReason](sessionId, s"$RegistrationReasonId", SellingGoodsAndServices)
      cacheSessionData[ConditionalDateFormElement](sessionId, s"$ThresholdInTwelveMonthsId", ConditionalDateFormElement(true, Some(LocalDate.now())))
      cacheSessionData[ConditionalDateFormElement](sessionId, s"$ThresholdNextThirtyDaysId", ConditionalDateFormElement(true, Some(LocalDate.now())))
      cacheSessionData[ConditionalDateFormElement](sessionId, s"$ThresholdPreviousThirtyDaysId", ConditionalDateFormElement(true, Some(LocalDate.now())))
      cacheSessionData[Boolean](sessionId, s"$VoluntaryRegistrationId", true)
      cacheSessionData[DateFormElement](sessionId, s"$DateOfBusinessTransferId", DateFormElement(LocalDate.now()))
      cacheSessionData[String](sessionId, s"$PreviousBusinessNameId", "test")
      cacheSessionData[String](sessionId, s"$VATNumberId", "test")
      cacheSessionData[Boolean](sessionId, s"$KeepOldVrnId", true)
      cacheSessionData[Boolean](sessionId, s"$TermsAndConditionsId", true)
      cacheSessionData[Boolean](sessionId, s"$TaxableSuppliesInUkId", true)
      cacheSessionData[Boolean](sessionId, s"$GoneOverThresholdId", true)
      cacheSessionData[DateFormElement](sessionId, s"$ThresholdTaxableSuppliesId", DateFormElement(LocalDate.now()))

      val request = buildClient("/registration-reason")
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> ukEstablishedOverseasExporterKey))

      val response = await(request)

      List(
        ThresholdInTwelveMonthsId, ThresholdNextThirtyDaysId, ThresholdNextThirtyDaysId, VoluntaryRegistrationId,
        DateOfBusinessTransferId, PreviousBusinessNameId, VATNumberId, KeepOldVrnId, TermsAndConditionsId,
        TaxableSuppliesInUkId, GoneOverThresholdId, ThresholdTaxableSuppliesId
      ).foreach(id =>
        verifySessionCacheData(sessionId, s"$id", None)
      )

      response.status mustBe SEE_OTHER
    }
  }
}
