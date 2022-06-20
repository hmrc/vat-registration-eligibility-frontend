package controllers

import helpers.{IntegrationSpecBase, S4LStub, TrafficManagementStub, VatRegistrationStub}
import identifiers._
import models._
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.http.cache.client.CacheMap

import java.time.LocalDate

class MtdInformationControllerISpec extends IntegrationSpecBase
  with S4LStub
  with VatRegistrationStub
  with TrafficManagementStub {

  val pageUrl = "/mtd-mandatory-information"

  "GET /mtd-mandatory-information" must {
    "return OK" in new Setup {
      stubSuccessfulLogin()
      stubAudits()

      val res = await(buildClient(pageUrl).get)

      res.status mustBe OK
    }
  }

  "POST /mtd-mandatory-information" must {
    "redirect back to VRS-FE to continue registration" in new Setup {
      stubS4LGetNothing(testRegId)
      stubSuccessfulLogin()
      stubAudits()
      cacheSessionData[Boolean](sessionId, FixedEstablishmentId, true)
      cacheSessionData[BusinessEntity](sessionId, BusinessEntityId, UKCompany)
      cacheSessionData[ConditionalDateFormElement](sessionId, ThresholdInTwelveMonthsId, ConditionalDateFormElement(value = false, None))
      cacheSessionData[ConditionalDateFormElement](sessionId, ThresholdNextThirtyDaysId, ConditionalDateFormElement(value = false, None))
      cacheSessionData[Boolean](sessionId, VoluntaryRegistrationId, true)
      cacheSessionData[Boolean](sessionId, CurrentlyTradingId, false)
      cacheSessionData[Boolean](sessionId, InternationalActivitiesId, false)
      cacheSessionData[Boolean](sessionId, InvolvedInOtherBusinessId, false)
      cacheSessionData[RegisteringBusiness](sessionId, RegisteringBusinessId, OwnBusiness)
      cacheSessionData[RegistrationReason](sessionId, RegistrationReasonId, SellingGoodsAndServices)
      cacheSessionData[Boolean](sessionId, AgriculturalFlatRateSchemeId, false)
      cacheSessionData[Boolean](sessionId, NinoId, true)
      cacheSessionData[Boolean](sessionId, RacehorsesId, false)

      stubSaveEligibilityData(testRegId)
      stubUpsertRegistrationInformation(testRegId)(RegistrationInformation(testInternalId, testRegId, Draft, Some(LocalDate.now), VatReg))
      stubS4LSave(testRegId, "eligibility-data")(CacheMap(testRegId, Map()))

      val res = await(buildClient(pageUrl).post(Json.obj()))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(s"/register-for-vat/journey/$testRegId")
      stubSuccessfulLogin()
      stubAudits()
    }
    "Return Internal Server Error if data is missing" in new Setup {
      stubS4LGetNothing(testRegId)
      stubSuccessfulLogin()
      stubAudits()
      stubSaveEligibilityData(testRegId)

      val res = await(buildClient(pageUrl).post(Json.obj()))

      res.status mustBe INTERNAL_SERVER_ERROR
    }
  }

}
