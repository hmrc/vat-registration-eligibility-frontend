

package controllers

import helpers.{AuthHelper, IntegrationSpecBase, SessionStub}
import identifiers.ThresholdNextThirtyDaysId
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.mvc.Http.HeaderNames

class ThresholdNextThirtyDaysISpec extends IntegrationSpecBase with AuthHelper with SessionStub {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build()

  val dateFieldName = s"${ThresholdNextThirtyDaysId}Date"

  s" ${controllers.routes.ThresholdNextThirtyDaysController.onSubmit()}" should {
    s"redirect to ${controllers.routes.VATRegistrationExceptionController.onPageLoad()} with value of true" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()

      val request = buildClient("/make-more-taxable-sales").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map(
          "value" -> Seq("true"),
          s"$dateFieldName.day" -> Seq("1"),
          s"$dateFieldName.month" -> Seq("1"),
          s"$dateFieldName.year" -> Seq("2020")
        ))
      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VATRegistrationExceptionController.onPageLoad().url)
    }
    s"redirect to ${controllers.routes.VoluntaryRegistrationController.onPageLoad()} with value of false" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()

      val request = buildClient("/make-more-taxable-sales").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map(
          "value" -> Seq("false"))
        )
      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VoluntaryRegistrationController.onPageLoad().url)
    }
  }
}
