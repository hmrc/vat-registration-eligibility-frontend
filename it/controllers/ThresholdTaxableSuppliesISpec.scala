

package controllers

import helpers.{AuthHelper, IntegrationSpecBase, SessionStub}
import identifiers.ThresholdTaxableSuppliesId
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.mvc.Http.HeaderNames

class ThresholdTaxableSuppliesISpec extends IntegrationSpecBase with AuthHelper with SessionStub {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build()

  val dateFieldName = s"${ThresholdTaxableSuppliesId}Date"

  s" ${controllers.routes.ThresholdTaxableSuppliesController.onSubmit()}" should {
    s"redirect to ${controllers.routes.VATExceptionKickoutController.onPageLoad} with value of true" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()

      val request = buildClient("/date-of-taxable-supplies-in-uk").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map(
          s"$dateFieldName.day" -> Seq("1"),
          s"$dateFieldName.month" -> Seq("1"),
          s"$dateFieldName.year" -> Seq("2020")
        ))
      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdTaxableSuppliesController.onPageLoad.url)
    }
  }
}
