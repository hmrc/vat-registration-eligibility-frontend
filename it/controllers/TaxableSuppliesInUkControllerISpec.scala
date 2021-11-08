package controllers

import featureswitch.core.config.FeatureSwitching
import helpers._
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, Json}
import play.mvc.Http.HeaderNames
import services.TrafficManagementService

import java.time.LocalDate

class TaxableSuppliesInUkControllerISpec extends IntegrationSpecBase
  with AuthHelper
  with SessionStub
  with FeatureSwitching
  with S4LStub {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build()

  val testDate: LocalDate = LocalDate.now

  val testEnrolments: JsArray = Json.arr(Json.obj(
    "key" -> TrafficManagementService.selfAssesmentEnrolment,
    "identifiers" -> Json.arr(
      Json.obj(
        "key" -> "testKey",
        "value" -> "testValue"
      )
    )
  ))

  val pageUrl: String = routes.TaxableSuppliesInUkController.onSubmit.toString

  s"POST $pageUrl" should {
    "redirect to Do Not Need To Register if the answer is no" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)

      val request = buildClient(pageUrl).withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map(
          "value" -> Seq("false"))
        )
      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.DoNotNeedToRegisterController.onPageLoad.url)
    }

    "redirect to the Traffic Management Resolver if the answer is yes" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)

      val request = buildClient(pageUrl).withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map(
          "value" -> Seq("true"))
        )
      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TrafficManagementResolverController.resolve.url)
    }
  }
}