package controllers

import helpers.{AuthHelper, IntegrationSpecBase, S4LStub, SessionStub}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, Json}
import play.mvc.Http.HeaderNames
import services.TrafficManagementService

import java.time.LocalDate

class NinoControllerISpec extends IntegrationSpecBase with AuthHelper with SessionStub with S4LStub {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build()

  val testDate: LocalDate = LocalDate.now

  val testEnrolments: JsArray = Json.arr(Json.obj(
    "key" -> TrafficManagementService.companyEnrolment,
    "identifiers" -> Json.arr(
      Json.obj(
        "key" -> "testKey",
        "value" -> "testValue"
      )
    )
  ))

  s"${controllers.routes.NinoController.onSubmit}" should {
    "redirect to VAT Exception if the answer is no" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)

      val request = buildClient("/have-nino").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map(
          "value" -> Seq("false"))
        )
      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VATExceptionKickoutController.onPageLoad.url)
    }

    "redirect to Traffic management resolver if the answer is yes" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)

      val request = buildClient("/have-nino").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map(
          "value" -> Seq("true"))
        )
      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TrafficManagementResolverController.resolve.url)
    }
  }
}