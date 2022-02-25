package controllers

import helpers._
import identifiers._
import models._
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.http.cache.client.CacheMap

import java.time.LocalDate

class EligibleControllerISpec extends IntegrationSpecBase
  with VatRegistrationStub
  with TrafficManagementStub
  with S4LStub {

  val testUrl: String = controllers.routes.EligibleController.onPageLoad.url

  "GET /eligible" must {
    "return OK" in new Setup {
      stubS4LGetNothing(testRegId)
      stubSuccessfulLogin()
      stubAudits()

      val res = await(buildClient(testUrl).get)

      res.status mustBe OK
    }
  }

  "POST /eligible" must {
    "Redirect to VAT reg frontend" in new Setup {
      stubS4LGetNothing(testRegId)
      stubSuccessfulLogin()
      stubAudits()
      cacheSessionData[Boolean](sessionId, FixedEstablishmentId, true)
      cacheSessionData[BusinessEntity](sessionId, BusinessEntityId, UKCompany)
      cacheSessionData[ConditionalDateFormElement](sessionId, ThresholdInTwelveMonthsId, ConditionalDateFormElement(value = false, None))
      cacheSessionData[ConditionalDateFormElement](sessionId, ThresholdNextThirtyDaysId, ConditionalDateFormElement(value = false, None))
      cacheSessionData[Boolean](sessionId, VoluntaryRegistrationId, true)
      cacheSessionData[TurnoverEstimateFormElement](sessionId, TurnoverEstimateId, TurnoverEstimateFormElement("50000"))
      cacheSessionData[Boolean](sessionId, InternationalActivitiesId, false)
      cacheSessionData[Boolean](sessionId, InvolvedInOtherBusinessId, false)
      cacheSessionData[Boolean](sessionId, ZeroRatedSalesId, false)
      cacheSessionData[RegisteringBusiness](sessionId, RegisteringBusinessId, OwnBusiness)
      cacheSessionData[RegistrationReason](sessionId, RegistrationReasonId, SellingGoodsAndServices)
      cacheSessionData[Boolean](sessionId, AgriculturalFlatRateSchemeId, false)
      cacheSessionData[Boolean](sessionId, NinoId, true)
      cacheSessionData[Boolean](sessionId, RacehorsesId, false)

      stubSaveEligibilityData(testRegId)
      stubUpsertRegistrationInformation(testRegId)(RegistrationInformation(testInternalId, testRegId, Draft, Some(LocalDate.now), VatReg))
      stubS4LSave(testRegId, "eligibility-data")(CacheMap(testRegId, Map()))

      val res = await(buildClient(testUrl).post(Map("value" -> Seq("false"))))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(s"/register-for-vat/journey/$testRegId")
    }

    "Return Internal Server Error if data is missing" in new Setup {
      stubS4LGetNothing(testRegId)
      stubSuccessfulLogin()
      stubAudits()
      stubSaveEligibilityData(testRegId)

      val res = await(buildClient(testUrl).post(Map("value" -> Seq("false"))))

      res.status mustBe INTERNAL_SERVER_ERROR
    }
  }

}
