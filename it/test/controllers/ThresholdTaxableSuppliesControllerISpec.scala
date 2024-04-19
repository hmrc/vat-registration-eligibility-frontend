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
import identifiers.ThresholdTaxableSuppliesId
import models.DateFormElement
import org.jsoup.Jsoup
import play.api.http.Status.SEE_OTHER
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

import java.time.LocalDate

class ThresholdTaxableSuppliesControllerISpec extends IntegrationSpecBase {

  val pageUrl = "/date-of-taxable-supplies-in-uk"
  val dateFieldName = s"${ThresholdTaxableSuppliesId}Date"
  val textbox = "value"
  val testDate = LocalDate.of(2022, 2, 23)

  "GET /date-of-taxable-supplies-in-uk" when {
    "an answer exists for the page" must {
      "return OK with the answer pre-populated" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        cacheSessionData(sessionIdStr, ThresholdTaxableSuppliesId, DateFormElement(testDate))

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

  "POST /date-of-taxable-supplies-in-uk" when {
    "the user provides a date" must {
      "redirect to the Gone Over Threshold page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient("/date-of-taxable-supplies-in-uk")
          .post(Map(
            s"$dateFieldName.day" -> Seq("1"),
            s"$dateFieldName.month" -> Seq("1"),
            s"$dateFieldName.year" -> Seq("2020")
          )))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.MtdInformationController.onPageLoad.url)
      }
    }
    "doesn't answer" must {
      "return BAD_REQUERST" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient("/date-of-taxable-supplies-in-uk").post(Map[String, String]()))

        res.status mustBe BAD_REQUEST
      }
    }
  }
}
