package controllers

import helpers._
import identifiers._
import models._
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.http.cache.client.CacheMap

import java.time.LocalDate

class EligibleControllerISpec extends IntegrationSpecBase
  with AuthHelper
  with SessionStub
  with VatRegistrationStub
  with TrafficManagementStub
  with S4LStub {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build()

  val testUrl: String = controllers.routes.EligibleController.onPageLoad.url

  val testInternalId = "testInternalId"

  "GET /eligible" must {
    "return OK" in {
      stubS4LGetNothing(testRegId)
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()

      val res = await(buildClient(testUrl).get)

      res.status mustBe OK
    }
  }

  "POST /eligible" must {
    "Redirect to VAT reg frontend" in {
      stubS4LGetNothing(testRegId)
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      cacheSessionData[Boolean](testInternalId, s"$FixedEstablishmentId", true)
      cacheSessionData[BusinessEntity](testInternalId, s"$BusinessEntityId", UKCompany)
      cacheSessionData[ConditionalDateFormElement](testInternalId, s"$ThresholdInTwelveMonthsId", ConditionalDateFormElement(value = false, None))
      cacheSessionData[ConditionalDateFormElement](testInternalId, s"$ThresholdNextThirtyDaysId", ConditionalDateFormElement(value = false, None))
      cacheSessionData[Boolean](testInternalId, s"$VoluntaryRegistrationId", true)
      cacheSessionData[TurnoverEstimateFormElement](testInternalId, s"$TurnoverEstimateId", TurnoverEstimateFormElement("50000"))
      cacheSessionData[Boolean](testInternalId, s"$InternationalActivitiesId", false)
      cacheSessionData[Boolean](testInternalId, s"$InvolvedInOtherBusinessId", false)
      cacheSessionData[Boolean](testInternalId, s"$AnnualAccountingSchemeId", false)
      cacheSessionData[Boolean](testInternalId, s"$ZeroRatedSalesId", false)
      cacheSessionData[Boolean](testInternalId, s"$RegisteringBusinessId", true)
      cacheSessionData[Boolean](testInternalId, s"$AgriculturalFlatRateSchemeId", false)
      cacheSessionData[Boolean](testInternalId, s"$NinoId", true)
      cacheSessionData[Boolean](testInternalId, s"$RacehorsesId", false)

      stubSaveEligibilityData(testRegId)
      stubUpsertRegistrationInformation(RegistrationInformation(testInternalId, testRegId, Draft, Some(LocalDate.now), VatReg))
      stubS4LSave(testRegId, "eligibility-data")(CacheMap(testRegId, Map()))
      val res = await(buildClient(testUrl)
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("false"))))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some("/register-for-vat/honesty-declaration")
    }

    "Return Internal Server Error if data is missing" in {
      stubS4LGetNothing(testRegId)
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubSaveEligibilityData(testRegId)

      val res = await(buildClient(testUrl)
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("false"))))

      res.status mustBe INTERNAL_SERVER_ERROR
    }
  }

}
