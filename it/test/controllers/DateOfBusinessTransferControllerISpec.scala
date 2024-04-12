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
import identifiers.DateOfBusinessTransferId
import models.DateFormElement
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.mvc.Http.HeaderNames

import java.time.LocalDate

class DateOfBusinessTransferControllerISpec extends IntegrationSpecBase {

  val pageUrl = "/date-of-transfer"
  val dateFieldName = "relevantDate"

  "GET /date-of-transfer" must {
    "render the page" when {
      "no prepop data is present in mongo" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).get())

        res.status mustBe OK
      }
      "prepop data is present in mongo" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        cacheSessionData[DateFormElement](sessionIdStr, DateOfBusinessTransferId, DateFormElement(LocalDate.of(2017, 12, 1)))

        val res = await(buildClient(pageUrl).get())
        res.status mustBe OK
      }
    }
  }

  "POST /date-of-transfer" must {
    "redirect to Previous Business Name" in new Setup {
      stubSuccessfulLogin()
      stubAudits()

      val res = await(buildClient(pageUrl)
        .post(Map(
          s"$dateFieldName.day" -> Seq("1"),
          s"$dateFieldName.month" -> Seq("1"),
          s"$dateFieldName.year" -> Seq("2020")
        )))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.PreviousBusinessNameController.onPageLoad.url)
    }
    "return a badrequest with form errors" when {
      "an invalid date is passed in" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl)
          .post(Map(
            s"$dateFieldName.day" -> Seq("1"),
            s"$dateFieldName.month" -> Seq("bad data"),
            s"$dateFieldName.year" -> Seq(s"${LocalDate.now().getYear}")
          )))

        res.status mustBe BAD_REQUEST
      }
    }
  }
}