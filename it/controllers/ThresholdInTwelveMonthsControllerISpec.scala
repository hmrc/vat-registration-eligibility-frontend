package controllers

import helpers._
import identifiers._
import models._
import org.jsoup.Jsoup
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ThresholdInTwelveMonthsControllerISpec extends IntegrationSpecBase with S4LStub {

  val selectionFieldName = "value"
  val dateFieldName = "valueDate"
  val pageHeading = "In any 12-month period has Test Company gone over the VAT-registration threshold?"
  val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
  val testDate = LocalDate.of(2017, 12, 1)

  s"GET ${controllers.routes.ThresholdInTwelveMonthsController.onPageLoad.url}" must {
    "render the page" when {
      "no prepop data is present in mongo" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        cacheSessionData[Boolean](sessionId, FixedEstablishmentId, true)
        cacheSessionData[RegisteringBusiness](sessionId, RegisteringBusinessId, OwnBusiness)

        val res = await(buildClient("/gone-over-threshold").get())
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.dateFieldContainsValue(dateFieldName, testDate, includeDay = false) mustBe false
      }

      "prepop data is present in mongo" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        cacheSessionData[Boolean](sessionId, FixedEstablishmentId, true)
        cacheSessionData[RegisteringBusiness](sessionId, RegisteringBusinessId, OwnBusiness)
        cacheSessionData[ConditionalDateFormElement](sessionId, ThresholdInTwelveMonthsId, ConditionalDateFormElement(true, Some(testDate)))

        val res = await(buildClient("/gone-over-threshold").get())
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.dateFieldContainsValue(dateFieldName, testDate, includeDay = false) mustBe true
      }
    }

    "redirect to introduction" when {
      "no data is present in mongo" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        val res = await(buildClient("/gone-over-threshold").get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.FixedEstablishmentController.onPageLoad.url)
      }
    }
  }

  s"POST ${controllers.routes.ThresholdInTwelveMonthsController.onSubmit().url}" must {
    val incorpDate = LocalDate.now().minusMonths(14)
    val dateBeforeIncorp = incorpDate.minusMonths(2)
    val dateAfterIncorp = incorpDate.plusMonths(2)

    "return a badrequest with form errors" when {
      "a date before the incorp date is passed in" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        val res = await(buildClient("/gone-over-threshold")
          .post(Map(
            "selectionFieldName" -> Seq("true"),
            s"$dateFieldName.month" -> Seq("q"),
            s"$dateFieldName.year" -> Seq(s"${dateBeforeIncorp.getYear}")
          )))

        res.status mustBe BAD_REQUEST
      }
    }
    s"redirect to ${controllers.routes.ThresholdPreviousThirtyDaysController.onPageLoad.url}" when {
      "yes and a valid date is given and should drop ThresholdNextThirtyDaysId data if present but not exception" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        cacheSessionData[ConditionalDateFormElement](sessionId, ThresholdNextThirtyDaysId, ConditionalDateFormElement(value = false, None))
        cacheSessionData[Boolean](sessionId, VoluntaryRegistrationId, true)
        cacheSessionData(sessionId, VATRegistrationExceptionId, true)

        val res = await(buildClient("/gone-over-threshold")
          .post(Map(
            selectionFieldName -> Seq("true"),
            s"$dateFieldName.month" -> Seq(s"${dateAfterIncorp.getMonthValue}"),
            s"$dateFieldName.year" -> Seq(s"${dateAfterIncorp.getYear}")
          )))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdPreviousThirtyDaysController.onPageLoad.url)

        verifySessionCacheData[ConditionalDateFormElement](
          sessionId,
          ThresholdInTwelveMonthsId,
          Some(ConditionalDateFormElement(value = true, Some(LocalDate.of(dateAfterIncorp.getYear, dateAfterIncorp.getMonthValue, 1))))
        )

        verifySessionCacheData(sessionId, VoluntaryRegistrationId, Option.empty[Boolean])
        verifySessionCacheData(sessionId, ThresholdNextThirtyDaysId, Option.empty[ConditionalDateFormElement])
        verifySessionCacheData(sessionId, VATRegistrationExceptionId, Some(true))
      }
    }
    s"redirect to ${controllers.routes.ThresholdNextThirtyDaysController.onPageLoad.url}" when {
      "no is submitted should drop ThresholdPreviousThirtyDaysId data and exception but not voluntary" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        cacheSessionData(sessionId, VoluntaryRegistrationId, true)
        cacheSessionData[ConditionalDateFormElement](sessionId, ThresholdPreviousThirtyDaysId, ConditionalDateFormElement(value = false, None))
        cacheSessionData(sessionId, VATRegistrationExceptionId, true)

        val res = await(buildClient("/gone-over-threshold").post(Map(selectionFieldName -> Seq("false"))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdNextThirtyDaysController.onPageLoad.url)

        verifySessionCacheData[ConditionalDateFormElement](sessionId, ThresholdInTwelveMonthsId, Some(ConditionalDateFormElement(false, None)))
        verifySessionCacheData(sessionId, VoluntaryRegistrationId, Some(true))
        verifySessionCacheData(sessionId, ThresholdPreviousThirtyDaysId, Option.empty[ConditionalDateFormElement])
        verifySessionCacheData(sessionId, VATRegistrationExceptionId, Option.empty[Boolean])
      }
    }
  }
}
