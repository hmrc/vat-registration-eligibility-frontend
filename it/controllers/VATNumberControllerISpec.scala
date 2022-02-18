package controllers

import featureswitch.core.config.FeatureSwitching
import helpers.{AuthHelper, IntegrationSpecBase, S4LStub, SessionStub}
import play.api.Application
import play.api.http.Status.OK
import play.api.inject.guice.GuiceApplicationBuilder
import play.mvc.Http.HeaderNames

class VATNumberControllerISpec extends IntegrationSpecBase with AuthHelper with SessionStub with FeatureSwitching with S4LStub {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build()

  class Setup extends SessionTest(app)

  val testVatNumber = "123456782"

  s"GET ${controllers.routes.VATNumberController.onPageLoad.url}" should {
    "render the page" when {
      "no prepop data is present in mongo" in new Setup {
        stubSuccessfulLogin()
        stubSuccessfulRegIdGet()
        stubAudits()

        val request = buildClient("/vat-number").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie()).get()
        val response = await(request)
        response.status mustBe OK
      }

      "prepop data is present in mongo" in new Setup {
        stubSuccessfulLogin()
        stubSuccessfulRegIdGet()
        stubAudits()

        cacheSessionData[String](sessionId, s"vatNumber", testVatNumber)

        val request = buildClient("/vat-number").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie()).get()
        val response = await(request)
        response.status mustBe OK
      }
    }

    s"POST ${controllers.routes.VATNumberController.onSubmit()}" should {
      "redirect to Keep existing VRN" in new Setup {
        stubSuccessfulLogin()
        stubSuccessfulRegIdGet()
        stubAudits()
        stubS4LGetNothing(testRegId)

        val request = buildClient("/vat-number")
          .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
          .post(Map("vatNumber" -> testVatNumber))

        val response = await(request)
        response.status mustBe 303
        response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.KeepOldVrnController.onPageLoad.url)
      }

      "return a BAD_REQUEST with form errors" in new Setup {
        stubSuccessfulLogin()
        stubSuccessfulRegIdGet()
        stubAudits()
        stubS4LGetNothing(testRegId)

        val request = buildClient("/vat-number")
          .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
          .post(Map("vatNumber" -> ""))

        val response = await(request)
        response.status mustBe 400
      }
    }
  }
}
