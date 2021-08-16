package controllers

import helpers.{AuthHelper, FakeTimeMachine, IntegrationSpecBase, SessionStub}
import identifiers.{ThresholdInTwelveMonthsId, ThresholdPreviousThirtyDaysId, VoluntaryRegistrationId}
import models.ConditionalDateFormElement
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames
import utils.TimeMachine

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ThresholdPreviousThirtyDaysISpec extends IntegrationSpecBase with AuthHelper with SessionStub {

  val selectionFieldName = "value"
  val dateFieldName = s"${ThresholdPreviousThirtyDaysId}Date"
  val internalId = "testInternalId"
  val pageHeading = "Has Test Company ever expected to go over the VAT-registration threshold in a single 30-day period?"
  val pageHeadingAfter17 = "Has Test Company ever expected to make more than £85,000 in VAT-taxable sales in a single 30-day period?"
  val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
  val localDate = LocalDate.of(2020, 1, 1)

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .overrides(bind[TimeMachine].to[FakeTimeMachine])
    .build()

  s"GET ${controllers.routes.ThresholdPreviousThirtyDaysController.onPageLoad().url}" should {
    "render the page" when {
      "no data is present in mongo" in {
        stubSuccessfulLogin()
        stubSuccessfulRegIdGet()
        stubAudits()

        val request = buildClient("/gone-over-threshold-period").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie()).get()
        val response = await(request)

        response.status mustBe OK
      }

      "data is present in mongo" in {
        stubSuccessfulLogin()
        stubSuccessfulRegIdGet()
        stubAudits()
        cacheSessionData(internalId, s"$ThresholdPreviousThirtyDaysId", ConditionalDateFormElement(true, Some(LocalDate.of(2017, 12, 1))))

        val request = buildClient("/gone-over-threshold-period").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie()).get()
        val response = await(request)

        response.status mustBe OK
      }
    }
  }

  s"POST ${controllers.routes.ThresholdPreviousThirtyDaysController.onSubmit().url}" should {
    val incorpDate = LocalDate.of(2020, 1, 1).minusMonths(14)
    val dateAfterIncorp = incorpDate.plusMonths(2)

    s"redirect to ${controllers.routes.VATRegistrationExceptionController.onPageLoad().url}" when {
      "yes and a valid date is submitted, and Q1 is yes should also drop voluntary" in {
        stubSuccessfulLogin()
        stubSuccessfulRegIdGet()
        stubAudits()
        cacheSessionData[ConditionalDateFormElement](internalId, ThresholdInTwelveMonthsId.toString, ConditionalDateFormElement(true, Some(localDate)))
        cacheSessionData[Boolean](internalId, VoluntaryRegistrationId.toString, true)

        val request = buildClient("/gone-over-threshold-period").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
          .post(Map(
            selectionFieldName -> Seq("true"),
            s"$dateFieldName.day" -> Seq(s"${dateAfterIncorp.getDayOfMonth}"),
            s"$dateFieldName.month" -> Seq(s"${dateAfterIncorp.getMonthValue}"),
            s"$dateFieldName.year" -> Seq(s"${dateAfterIncorp.getYear}")
          ))

        val response = await(request)
        response.status mustBe SEE_OTHER
        response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VATRegistrationExceptionController.onPageLoad().url)
        verifySessionCacheData[ConditionalDateFormElement](internalId, ThresholdPreviousThirtyDaysId.toString, Some(ConditionalDateFormElement(true, Some(dateAfterIncorp))))
        verifySessionCacheData[ConditionalDateFormElement](internalId, ThresholdInTwelveMonthsId.toString, Some(ConditionalDateFormElement(true, Some(localDate))))
        verifySessionCacheData(internalId, VoluntaryRegistrationId.toString, Option.empty[Boolean])
      }
      "no is submitted" in {
        stubSuccessfulLogin()
        stubSuccessfulRegIdGet()
        stubAudits()

        cacheSessionData[ConditionalDateFormElement](internalId, ThresholdInTwelveMonthsId.toString, ConditionalDateFormElement(true, Some(localDate)))

        val request = buildClient("/gone-over-threshold-period").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
          .post(Map(
            selectionFieldName -> Seq("false")
          ))

        val response = await(request)
        response.status mustBe 303
        response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VATRegistrationExceptionController.onPageLoad().url)

        verifySessionCacheData[ConditionalDateFormElement](internalId, ThresholdPreviousThirtyDaysId.toString, Some(ConditionalDateFormElement(false, None)))
        verifySessionCacheData[ConditionalDateFormElement](internalId, ThresholdInTwelveMonthsId.toString, Some(ConditionalDateFormElement(true, Some(localDate))))
      }
    }
  }
}
