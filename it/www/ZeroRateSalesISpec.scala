package www

import helpers.{AuthHelper, IntegrationSpecBase, SessionStub}
import identifiers.{AgriculturalFlatRateSchemeId, VATExemptionId, ZeroRatedSalesId}
import play.api.libs.json.Format._
import play.api.test.FakeApplication
import play.mvc.Http.HeaderNames

class ZeroRateSalesISpec extends IntegrationSpecBase with AuthHelper with SessionStub {

  override implicit lazy val app = FakeApplication(additionalConfiguration = fakeConfig())
  val internalId = "testInternalId"
  s"POST ${controllers.routes.ZeroRatedSalesController.onSubmit().url}" should {
    "navigate to Agriculture when false and no data exists in Exemption" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubSuccessfulTxIdGet()
      stubSuccessfulIncorpDataGet()
      stubSuccessfulCompanyNameGet()
      stubAudits()

      val request = buildClient(controllers.routes.ZeroRatedSalesController.onSubmit().url)
        .withHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("false")))

      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.AgriculturalFlatRateSchemeController.onPageLoad().url)
      verifySessionCacheData(internalId, ZeroRatedSalesId.toString, Option.apply[Boolean](false))
    }
    "navigate to Agriculture when false and data exists for Exemption" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubSuccessfulTxIdGet()
      stubSuccessfulCompanyNameGet()
      stubSuccessfulIncorpDataGet()
      stubAudits()
      cacheSessionData(internalId, VATExemptionId.toString, true)

      val request = buildClient(controllers.routes.ZeroRatedSalesController.onSubmit().url)
        .withHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("false")))

      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.AgriculturalFlatRateSchemeController.onPageLoad().url)
      verifySessionCacheData(internalId, ZeroRatedSalesId.toString, Option.apply[Boolean](false))
      verifySessionCacheData(internalId, VATExemptionId.toString, Option.empty[Boolean])
    }

    "navigate to VAT Exemption when true and no Agriculture data" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubSuccessfulTxIdGet()
      stubSuccessfulCompanyNameGet()
      stubSuccessfulIncorpDataGet()
      stubAudits()

      val request = buildClient(controllers.routes.ZeroRatedSalesController.onSubmit().url)
        .withHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("true")))

      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VATExemptionController.onPageLoad().url)
      verifySessionCacheData(internalId, ZeroRatedSalesId.toString, Option.apply[Boolean](true))
    }

    "navigate to VAT Exemption when true and Agriculture data exists" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubSuccessfulTxIdGet()
      stubSuccessfulCompanyNameGet()
      stubSuccessfulIncorpDataGet()
      stubAudits()
      cacheSessionData(internalId, AgriculturalFlatRateSchemeId.toString, true)

      val request = buildClient(controllers.routes.ZeroRatedSalesController.onSubmit().url)
        .withHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("true")))

      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VATExemptionController.onPageLoad().url)
      verifySessionCacheData(internalId, ZeroRatedSalesId.toString, Option.apply[Boolean](true))
      verifySessionCacheData(internalId, AgriculturalFlatRateSchemeId.toString, Option.empty[Boolean])
    }
  }
}