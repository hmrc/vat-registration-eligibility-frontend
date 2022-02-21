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

  class Setup extends SessionTest(app)

  val testUrl: String = controllers.routes.EligibleController.onPageLoad.url

  "GET /eligible" must {
    "return OK" in new Setup {
      stubS4LGetNothing(testRegId)
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()

      val res = await(buildClient(testUrl)
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .get)

      res.status mustBe OK
    }
  }

  "POST /eligible" must {
    "Redirect to VAT reg frontend" in new Setup {
      stubS4LGetNothing(testRegId)
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      cacheSessionData[Boolean](sessionId, s"$FixedEstablishmentId", true)
      cacheSessionData[BusinessEntity](sessionId, s"$BusinessEntityId", UKCompany)
      cacheSessionData[ConditionalDateFormElement](sessionId, s"$ThresholdInTwelveMonthsId", ConditionalDateFormElement(value = false, None))
      cacheSessionData[ConditionalDateFormElement](sessionId, s"$ThresholdNextThirtyDaysId", ConditionalDateFormElement(value = false, None))
      cacheSessionData[Boolean](sessionId, s"$VoluntaryRegistrationId", true)
      cacheSessionData[TurnoverEstimateFormElement](sessionId, s"$TurnoverEstimateId", TurnoverEstimateFormElement("50000"))
      cacheSessionData[Boolean](sessionId, s"$InternationalActivitiesId", false)
      cacheSessionData[Boolean](sessionId, s"$InvolvedInOtherBusinessId", false)
      cacheSessionData[Boolean](sessionId, s"$ZeroRatedSalesId", false)
      cacheSessionData[RegisteringBusiness](sessionId, s"$RegisteringBusinessId", OwnBusiness)
      cacheSessionData[RegistrationReason](sessionId, s"$RegistrationReasonId", SellingGoodsAndServices)
      cacheSessionData[Boolean](sessionId, s"$AgriculturalFlatRateSchemeId", false)
      cacheSessionData[Boolean](sessionId, s"$NinoId", true)
      cacheSessionData[Boolean](sessionId, s"$RacehorsesId", false)

      stubSaveEligibilityData(testRegId)
      stubUpsertRegistrationInformation(testRegId)(RegistrationInformation(testInternalId, testRegId, Draft, Some(LocalDate.now), VatReg))
      stubS4LSave(testRegId, "eligibility-data")(CacheMap(testRegId, Map()))
      val res = await(buildClient(testUrl)
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("false"))))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(s"/register-for-vat/journey/$testRegId")
    }

    "Return Internal Server Error if data is missing" in new Setup {
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
