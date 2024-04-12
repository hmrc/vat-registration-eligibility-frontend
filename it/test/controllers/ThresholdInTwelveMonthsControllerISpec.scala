/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

        cacheSessionData[Boolean](sessionIdStr, FixedEstablishmentId, true)
        cacheSessionData[RegisteringBusiness](sessionIdStr, RegisteringBusinessId, OwnBusiness)

        val res = await(buildClient("/gone-over-threshold").get())
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.dateFieldContainsValue(dateFieldName, testDate, includeDay = false) mustBe false
      }

      "prepop data is present in mongo" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        cacheSessionData[Boolean](sessionIdStr, FixedEstablishmentId, true)
        cacheSessionData[RegisteringBusiness](sessionIdStr, RegisteringBusinessId, OwnBusiness)
        cacheSessionData[ConditionalDateFormElement](sessionIdStr, ThresholdInTwelveMonthsId, ConditionalDateFormElement(true, Some(testDate)))

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

        cacheSessionData[ConditionalDateFormElement](sessionIdStr, ThresholdNextThirtyDaysId, ConditionalDateFormElement(value = false, None))
        cacheSessionData[Boolean](sessionIdStr, VoluntaryRegistrationId, true)
        cacheSessionData(sessionIdStr, VATRegistrationExceptionId, true)

        val res = await(buildClient("/gone-over-threshold")
          .post(Map(
            selectionFieldName -> Seq("true"),
            s"$dateFieldName.month" -> Seq(s"${dateAfterIncorp.getMonthValue}"),
            s"$dateFieldName.year" -> Seq(s"${dateAfterIncorp.getYear}")
          )))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdPreviousThirtyDaysController.onPageLoad.url)

        verifySessionCacheData[ConditionalDateFormElement](
          sessionIdStr,
          ThresholdInTwelveMonthsId,
          Some(ConditionalDateFormElement(value = true, Some(LocalDate.of(dateAfterIncorp.getYear, dateAfterIncorp.getMonthValue, 1))))
        )

        verifySessionCacheData(sessionIdStr, VoluntaryRegistrationId, Option.empty[Boolean])
        verifySessionCacheData(sessionIdStr, ThresholdNextThirtyDaysId, Option.empty[ConditionalDateFormElement])
        verifySessionCacheData(sessionIdStr, VATRegistrationExceptionId, Some(true))
      }
    }
    s"redirect to ${controllers.routes.ThresholdNextThirtyDaysController.onPageLoad.url}" when {
      "no is submitted should drop ThresholdPreviousThirtyDaysId data and exception but not voluntary" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        cacheSessionData(sessionIdStr, VoluntaryRegistrationId, true)
        cacheSessionData[ConditionalDateFormElement](sessionIdStr, ThresholdPreviousThirtyDaysId, ConditionalDateFormElement(value = false, None))
        cacheSessionData(sessionIdStr, VATRegistrationExceptionId, true)

        val res = await(buildClient("/gone-over-threshold").post(Map(selectionFieldName -> Seq("false"))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdNextThirtyDaysController.onPageLoad.url)

        verifySessionCacheData[ConditionalDateFormElement](sessionIdStr, ThresholdInTwelveMonthsId, Some(ConditionalDateFormElement(false, None)))
        verifySessionCacheData(sessionIdStr, VoluntaryRegistrationId, Some(true))
        verifySessionCacheData(sessionIdStr, ThresholdPreviousThirtyDaysId, Option.empty[ConditionalDateFormElement])
        verifySessionCacheData(sessionIdStr, VATRegistrationExceptionId, Option.empty[Boolean])
      }
    }
  }
}
