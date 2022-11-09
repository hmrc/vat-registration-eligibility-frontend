package controllers

import helpers.IntegrationSpecBase
import identifiers.{ThresholdInTwelveMonthsId, ThresholdPreviousThirtyDaysId, VoluntaryRegistrationId}
import models.ConditionalDateFormElement
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ThresholdPreviousThirtyDaysControllerISpec extends IntegrationSpecBase {

  val pageUrl = "/gone-over-threshold-period"
  val selectionFieldName = "value"
  val dateFieldName = s"${ThresholdPreviousThirtyDaysId}Date"
  val internalId = "testInternalId"
  val pageHeading = "Has Test Company ever expected to go over the VAT-registration threshold in a single 30-day period?"
  val pageHeadingAfter17 = "Has Test Company ever expected to make more than £85,000 in VAT-taxable sales in a single 30-day period?"
  val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
  val localDate = LocalDate.of(2020, 1, 1)

  s"GET ${controllers.routes.ThresholdPreviousThirtyDaysController.onPageLoad.url}" must {
    "render the page" when {
      "no data is present in mongo" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).get)

        res.status mustBe OK
      }

      "data is present in mongo" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        cacheSessionData(sessionId, ThresholdPreviousThirtyDaysId, ConditionalDateFormElement(true, Some(LocalDate.of(2017, 12, 1))))

        val res = await(buildClient(pageUrl).get)

        res.status mustBe OK
      }
    }
  }

  s"POST ${controllers.routes.ThresholdPreviousThirtyDaysController.onSubmit().url}" when {
    val incorpDate = LocalDate.of(2020, 1, 1).minusMonths(14)
    val dateAfterIncorp = incorpDate.plusMonths(2)

    s"redirect to ${controllers.routes.VATRegistrationExceptionController.onPageLoad.url}" when {
      "yes and a valid date is submitted, and Q1 is yes should also drop voluntary" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        cacheSessionData[ConditionalDateFormElement](sessionId, ThresholdInTwelveMonthsId, ConditionalDateFormElement(true, Some(localDate)))
        cacheSessionData[Boolean](sessionId, VoluntaryRegistrationId, true)

          val res = await(buildClient(pageUrl)
            .post(Map(
              selectionFieldName -> Seq("true"),
              s"$dateFieldName.day" -> Seq(s"${dateAfterIncorp.getDayOfMonth}"),
              s"$dateFieldName.month" -> Seq(s"${dateAfterIncorp.getMonthValue}"),
              s"$dateFieldName.year" -> Seq(s"${dateAfterIncorp.getYear}")
            )))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VATRegistrationExceptionController.onPageLoad.url)

        verifySessionCacheData[ConditionalDateFormElement](sessionId, ThresholdPreviousThirtyDaysId, Some(ConditionalDateFormElement(true, Some(dateAfterIncorp))))
        verifySessionCacheData[ConditionalDateFormElement](sessionId, ThresholdInTwelveMonthsId, Some(ConditionalDateFormElement(true, Some(localDate))))
        verifySessionCacheData(sessionId, VoluntaryRegistrationId, Option.empty[Boolean])
      }
      "no is submitted" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

          cacheSessionData[ConditionalDateFormElement](sessionId, ThresholdInTwelveMonthsId, ConditionalDateFormElement(true, Some(localDate)))

          val res = await(buildClient(pageUrl).post(Map(selectionFieldName -> Seq("false"))))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VATRegistrationExceptionController.onPageLoad.url)

          verifySessionCacheData[ConditionalDateFormElement](sessionId, ThresholdPreviousThirtyDaysId, Some(ConditionalDateFormElement(false, None)))
          verifySessionCacheData[ConditionalDateFormElement](sessionId, ThresholdInTwelveMonthsId, Some(ConditionalDateFormElement(true, Some(localDate))))
        }
      }
    "the user doesn't answer" must {
      "return BAD_REQUEST" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map[String, String]()))

        res.status mustBe BAD_REQUEST
      }
    }
  }
}
