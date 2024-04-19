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

import helpers.IntegrationSpecBase
import identifiers.ThresholdNextThirtyDaysId
import models.ConditionalDateFormElement
import org.jsoup.Jsoup
import play.api.http.Status._
import play.mvc.Http.HeaderNames

import java.time.LocalDate

class ThresholdNextThirtyDaysControllerISpec extends IntegrationSpecBase {

  val dateFieldName = s"${ThresholdNextThirtyDaysId}Date"

  val pageUrl = "/make-more-taxable-sales"
  val textbox = "value"
  val testDate = LocalDate.of(2022, 2, 23)

  "GET /make-more-taxable-sales" when {
    "an answer exists for the page" must {
      "return OK with the answer pre-populated" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        cacheSessionData(sessionIdStr, ThresholdNextThirtyDaysId, ConditionalDateFormElement(true, Some(testDate)))

        val res = await(buildClient(pageUrl).get)
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.dateFieldContainsValue(textbox, testDate)
      }
    }
    "an answer doesn't exist for the page" must {
      "return OK with an empty form" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).get)

        res.status mustBe OK
      }
    }
  }

  s"POST /make-more-taxable-sales" when {
    "the user answers" must {
      s"redirect to ${controllers.routes.VATRegistrationExceptionController.onPageLoad} with value of true" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient("/make-more-taxable-sales")
          .post(Map(
            "value" -> Seq("true"),
            s"$dateFieldName.day" -> Seq("1"),
            s"$dateFieldName.month" -> Seq("1"),
            s"$dateFieldName.year" -> Seq("2020")
          )))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VATRegistrationExceptionController.onPageLoad.url)
      }
      s"redirect to ${controllers.routes.VoluntaryRegistrationController.onPageLoad} with value of false" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient("/make-more-taxable-sales").post(Map("value" -> Seq("false"))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VoluntaryRegistrationController.onPageLoad.url)
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
