package www

import helpers.{AuthHelper, IntegrationSpecBase, SessionStub, TrafficManagementStub}
import identifiers.{ThresholdInTwelveMonthsId, ThresholdNextThirtyDaysId, VATRegistrationExceptionId, VoluntaryRegistrationId}
import models.{ConditionalDateFormElement, Draft, RegistrationInformation, VatReg}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ThresholdInTwelveMonthsControllerISpec extends IntegrationSpecBase with AuthHelper with SessionStub with TrafficManagementStub {

  val selectionFieldName = "value"
  val dateFieldName = "valueDate"
  val internalId = "testInternalId"
  val pageHeading = "In any 12-month period has Test Company gone over the VAT-registration threshold?"
  val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build

  s"GET ${controllers.routes.ThresholdInTwelveMonthsController.onPageLoad().url}" should {
    "render the page" when {
      "no data is present in mongo" in {
        stubSuccessfulLogin()
        stubSuccessfulRegIdGet()
        stubAudits()

        val request = buildClient("/gone-over-threshold").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie()).get()
        val response = await(request)
        response.status mustBe OK
      }

      "data is present in mongo" in {
        stubSuccessfulLogin()
        stubSuccessfulRegIdGet()
        stubAudits()

        cacheSessionData[ConditionalDateFormElement](internalId, s"$ThresholdInTwelveMonthsId", ConditionalDateFormElement(true, Some(LocalDate.of(2017, 12, 1))))

        val request = buildClient("/gone-over-threshold").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie()).get()
        val response = await(request)
        response.status mustBe OK
      }
    }
  }

  s"POST ${controllers.routes.ThresholdInTwelveMonthsController.onSubmit().url}" should {
    val incorpDate = LocalDate.now().minusMonths(14)
    val dateBeforeIncorp = incorpDate.minusMonths(2)
    val dateAfterIncorp = incorpDate.plusMonths(2)

    "return a badrequest with form errors" when {
      "a date before the incorp date is passed in" in {
        stubSuccessfulLogin()
        stubSuccessfulRegIdGet()
        stubAudits()

        val request = buildClient("/gone-over-threshold").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
          .post(Map(
            "selectionFieldName" -> Seq("true"),
            s"$dateFieldName.month" -> Seq("q"),
            s"$dateFieldName.year" -> Seq(s"${dateBeforeIncorp.getYear}")
          ))

        val response = await(request)
        response.status mustBe 400
      }
    }
    s"redirect to ${controllers.routes.ThresholdPreviousThirtyDaysController.onPageLoad().url}" when {
      "yes and a valid date is given and should drop ThresholdNextThirtyDaysId data if present but not exception" in {
        stubSuccessfulLogin()
        stubSuccessfulRegIdGet()
        stubAudits()
        stubUpsertRegistrationInformation(RegistrationInformation("testInternalId", "testRegId", Draft, Some(LocalDate.now), VatReg))

        cacheSessionData[ConditionalDateFormElement](internalId, ThresholdNextThirtyDaysId.toString, ConditionalDateFormElement(false, None))
        cacheSessionData[Boolean](internalId, VoluntaryRegistrationId.toString, true)
        cacheSessionData(internalId, VATRegistrationExceptionId.toString, true)

        val request = buildClient("/gone-over-threshold").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
          .post(Map(
            selectionFieldName -> Seq("true"),
            s"$dateFieldName.month" -> Seq(s"${dateAfterIncorp.getMonthValue}"),
            s"$dateFieldName.year" -> Seq(s"${dateAfterIncorp.getYear}")
          ))

        val response = await(request)
        response.status mustBe 303
        response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdPreviousThirtyDaysController.onPageLoad().url)
        verifySessionCacheData(internalId, VoluntaryRegistrationId.toString, Option.empty[Boolean])
        verifySessionCacheData[ConditionalDateFormElement](internalId, ThresholdInTwelveMonthsId.toString,
          Some(ConditionalDateFormElement(true, Some(LocalDate.of(dateAfterIncorp.getYear, dateAfterIncorp.getMonthValue, 1)))))
        verifySessionCacheData(internalId, ThresholdNextThirtyDaysId.toString, Option.empty[Boolean])
        verifySessionCacheData(internalId, VATRegistrationExceptionId.toString, Some(true))
      }
    }
    s"redirect to ${controllers.routes.ThresholdNextThirtyDaysController.onPageLoad().url}" when {
      "no is submitted should not drop ThresholdNextThirtyDaysId or voluntary but drop exception" in {
        stubSuccessfulLogin()
        stubSuccessfulRegIdGet()
        stubAudits()

        stubUpsertRegistrationInformation(RegistrationInformation("testInternalId", "testRegId", Draft, Some(LocalDate.now), VatReg))

        cacheSessionData(internalId, VoluntaryRegistrationId.toString, true)
        cacheSessionData[Boolean](internalId, ThresholdNextThirtyDaysId.toString, false)
        cacheSessionData(internalId, VATRegistrationExceptionId.toString, true)

        val request = buildClient("/gone-over-threshold").withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
          .post(Map(
            selectionFieldName -> Seq("false")
          ))

        val response = await(request)
        response.status mustBe 303
        response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdNextThirtyDaysController.onPageLoad().url)
        verifySessionCacheData(internalId, VoluntaryRegistrationId.toString, Some(true))
        verifySessionCacheData(internalId, ThresholdNextThirtyDaysId.toString, Some(false))
        verifySessionCacheData[ConditionalDateFormElement](internalId, ThresholdInTwelveMonthsId.toString, Some(ConditionalDateFormElement(false, None)))
        verifySessionCacheData(internalId, VATRegistrationExceptionId.toString, Option.empty[Boolean])
      }
    }
  }
}
