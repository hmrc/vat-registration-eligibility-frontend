package controllers

import helpers._
import identifiers.DateOfBusinessTransferId
import models.DateFormElement
import play.api.Application
import play.api.http.Status.OK
import play.api.inject.guice.GuiceApplicationBuilder
import play.mvc.Http.HeaderNames

import java.time.LocalDate

class DateOfBusinessTransferControllerISpec extends IntegrationSpecBase with AuthHelper with SessionStub {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build()

  val dateFieldName = "relevantDate"

  class Setup extends SessionTest(app)

  s"GET ${controllers.routes.ThresholdInTwelveMonthsController.onPageLoad.url}" should {
    "render the page" when {
      "no prepop data is present in mongo" in new Setup {
        stubSuccessfulLogin()
        stubSuccessfulRegIdGet()
        stubAudits()

        val request = buildClient("/date-of-transfer").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie()).get()
        val response = await(request)
        response.status mustBe OK
      }
      "prepop data is present in mongo" in new Setup {
        stubSuccessfulLogin()
        stubSuccessfulRegIdGet()
        stubAudits()

        cacheSessionData[DateFormElement](sessionId, s"$DateOfBusinessTransferId", DateFormElement(LocalDate.of(2017, 12, 1)))

        val request = buildClient("/date-of-transfer").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie()).get()
        val response = await(request)
        response.status mustBe OK
      }
    }
  }

  s"POST ${controllers.routes.DateOfBusinessTransferController.onSubmit()}" should {

    s"redirect to Previous Business Name" in new Setup {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()

      val request = buildClient("/date-of-transfer").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map(
          s"$dateFieldName.day" -> Seq("1"),
          s"$dateFieldName.month" -> Seq("1"),
          s"$dateFieldName.year" -> Seq("2020")
        ))
      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.PreviousBusinessNameController.onPageLoad.url)
    }
    "return a badrequest with form errors" when {
      "an invalid date is passed in" in new Setup {
        stubSuccessfulLogin()
        stubSuccessfulRegIdGet()
        stubAudits()

        val request = buildClient("/date-of-transfer").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
          .post(Map(
            s"$dateFieldName.day" -> Seq("1"),
            s"$dateFieldName.month" -> Seq("bad data"),
            s"$dateFieldName.year" -> Seq(s"${LocalDate.now().getYear}")
          ))

        val response = await(request)
        response.status mustBe 400
      }
    }
  }
}