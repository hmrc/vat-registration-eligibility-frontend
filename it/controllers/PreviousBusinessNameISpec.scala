package controllers

import featureswitch.core.config.FeatureSwitching
import helpers._
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.mvc.Http.HeaderNames

class PreviousBusinessNameISpec extends IntegrationSpecBase with AuthHelper with SessionStub with FeatureSwitching with S4LStub {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build()

  val testPreviousBusinessName = "Al Pacino Ltd"

  class Setup extends SessionTest(app)

  s"GET ${controllers.routes.PreviousBusinessNameController.onPageLoad.url}" must {
    "return OK" in new Setup {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)

      val res = await(buildClient("/previous-business-name")
          .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
          .get
      )

      res.status mustBe OK
    }
  }

  s"POST ${controllers.routes.PreviousBusinessNameController.onSubmit()}" should {
    "redirect to Previous VRN" in new Setup {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)

      val request = buildClient("/previous-business-name")
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("previousBusinessName" -> testPreviousBusinessName))

      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VATNumberController.onPageLoad.url)
    }
    "return a BAD_REQUEST with form errors" in new Setup {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)

      val request = buildClient("/previous-business-name")
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("previousBusinessName" -> ""))

      val response = await(request)
      response.status mustBe 400
    }
  }
}