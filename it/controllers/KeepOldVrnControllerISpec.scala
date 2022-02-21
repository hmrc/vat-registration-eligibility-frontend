package controllers

import helpers.{AuthHelper, IntegrationSpecBase, S4LStub, SessionStub}
import identifiers.TermsAndConditionsId
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.mvc.Http.HeaderNames

class KeepOldVrnControllerISpec extends IntegrationSpecBase with AuthHelper with SessionStub with S4LStub {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build()

  class Setup extends SessionTest(app)

  s"GET ${controllers.routes.KeepOldVrnController.onPageLoad.url}" must {
    "return OK" in new Setup {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)

      val res = await(buildClient("/keep-old-vrn")
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .get
      )

      res.status mustBe OK
    }
  }

  s"POST ${controllers.routes.KeepOldVrnController.onSubmit}" should {
    "redirect to Turnover Estimate and clear down old T&C answer if the answer is no" in new Setup {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)
      cacheSessionData[Boolean](sessionId, s"$TermsAndConditionsId", true)

      val request = buildClient("/keep-old-vrn")
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("false")))

      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TurnoverEstimateController.onPageLoad.url)
      verifySessionCacheData[Boolean](sessionId, s"$TermsAndConditionsId", None)
    }

    "redirect to Terms & Conditions page if the answer is yes" in new Setup {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)

      val request = buildClient("/keep-old-vrn").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("true")))

      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TermsAndConditionsController.onPageLoad.url)
    }
  }
}