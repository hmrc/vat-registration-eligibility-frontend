/*
 * Copyright 2017 HM Revenue & Customs
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

import java.time.LocalDate

import fixtures.{S4LFixture, VatRegistrationFixture}
import helpers.VatRegSpec
import mocks.ThresholdServiceMock
import models.CurrentProfile
import models.MonthYearModel.FORMAT_DD_MMMM_Y
import models.view.{ExpectationOverThresholdView, OverThresholdView}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import play.api.http.Status
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class ThresholdControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LFixture with ThresholdServiceMock {

  val expectedError = "Date of Incorporation data expected to be found in Incorporation"

  val testThresholdController = new ThresholdController {
    override val authConnector = mockAuthConnector
    override val thresholdService = mockThresholdService
    override val messagesApi = mockMessages
    override val currentProfileService = mockCurrentProfileService

    override def withCurrentProfile(f: (CurrentProfile) => Future[Result])(implicit request: Request[_], hc: HeaderCarrier): Future[Result] = {
     f(currentProfile)
    }
  }

  val testThresholdControllerWithoutIncorpDate = new ThresholdController {
    override val authConnector = mockAuthConnector
    override val thresholdService = mockThresholdService
    override val messagesApi = mockMessagesApi
    override val currentProfileService = mockCurrentProfileService

    override def withCurrentProfile(f: (CurrentProfile) => Future[Result])(implicit request: Request[_], hc: HeaderCarrier): Future[Result] = {
      f(currentProfile.copy(incorporationDate = None))
    }
  }

  val fakeRequest = FakeRequest(routes.ThresholdController.goneOverShow())

  s"GET ${routes.ThresholdController.goneOverShow()}" should {
    val expectedText = "VAT taxable turnover gone over"

    "returnException if no IncorporationInfo Date present" in {
      val res = intercept[Exception](callAuthorised(testThresholdControllerWithoutIncorpDate.goneOverShow) {
        a => status(a) shouldBe 500
      }).getMessage
      res shouldBe expectedError
    }

    "return HTML when there is no view data" in {
      mockGetThresholdViewModel[OverThresholdView](Future.successful(None))

      callAuthorised(testThresholdController.goneOverShow) { res =>
        res includesText expectedText
        res passJsoupTest { doc =>
          doc.getElementById("pageHeading").text should include(incorpDate.format(FORMAT_DD_MMMM_Y))
          doc.getElementsByAttribute("checked").size shouldBe 0
          doc.getElementById("overThreshold.month").attr("value") shouldBe ""
          doc.getElementById("overThreshold.year").attr("value") shouldBe ""
        }
      }
    }

    "return HTML when there's an over threshold view data with date" in {
      mockGetThresholdViewModel[OverThresholdView](Future.successful(Some(validOverThresholdView.copy(true, Some(LocalDate.of(2017, 6, 30))))))

      callAuthorised(testThresholdController.goneOverShow) {
        _ passJsoupTest { doc =>
          doc.getElementById("overThreshold.month").attr("value") shouldBe "6"
          doc.getElementById("overThreshold.year").attr("value") shouldBe "2017"
          doc.getElementById("overThresholdRadio-true").attr("checked") shouldBe "checked"
        }
      }
    }

    "return HTML when there's an over threshold view data with no date" in {
      mockGetThresholdViewModel[OverThresholdView](Future.successful(Some(validOverThresholdView.copy(false, None))))

      callAuthorised(testThresholdController.goneOverShow) {
        _ passJsoupTest { doc =>
          doc.getElementById("overThreshold.month").attr("value") shouldBe ""
          doc.getElementById("overThreshold.year").attr("value") shouldBe ""
          doc.getElementById("overThresholdRadio-false").attr("checked") shouldBe "checked"
        }
      }
    }
  }

  s"POST ${routes.ThresholdController.goneOverSubmit()}" should {
    "return Exception When Incorporation date is empty" in {

      val res = intercept[Exception](submitAuthorised
      (testThresholdControllerWithoutIncorpDate.goneOverSubmit, fakeRequest.withFormUrlEncodedBody())
      (a => status(a) shouldBe 500)).getMessage
      res shouldBe expectedError
    }

    "return 400 when no data posted" in {

      submitAuthorised(
        testThresholdController.goneOverSubmit(), fakeRequest.withFormUrlEncodedBody()) {
        status(_) shouldBe Status.BAD_REQUEST
      }
    }

    "return 400 when partial data is posted" in {

      submitAuthorised(
        testThresholdController.goneOverSubmit(), fakeRequest.withFormUrlEncodedBody(
          "overThresholdRadio" -> "true",
          "overThreshold.month" -> "",
          "overThreshold.year" -> "2017"
        )) {
        status(_) shouldBe Status.BAD_REQUEST
      }
    }

    "return 400 with incorrect data (date before incorporation date) - yes selected" in {
      val thresholdOverThresholdTrue = validThresholdPostIncorp.copy(overThreshold = Some(OverThresholdView(true, Some(LocalDate.of(2017, 6, 30)))))
      mockSaveThreshold(Future.successful(thresholdOverThresholdTrue))

      submitAuthorised(testThresholdController.goneOverSubmit(), fakeRequest.withFormUrlEncodedBody(
        "overThresholdRadio" -> "true",
        "overThreshold.month" -> "6",
        "overThreshold.year" -> "2016"
      )) {
        status(_) shouldBe Status.BAD_REQUEST
      }
    }

    "return 303 with valid data - yes selected" in {
      val thresholdOverThresholdTrue = validThresholdPostIncorp.copy(overThreshold = Some(OverThresholdView(true, Some(LocalDate.of(2017, 1, 30)))))
      mockSaveThreshold(Future.successful(thresholdOverThresholdTrue))

      submitAuthorised(testThresholdController.goneOverSubmit(), fakeRequest.withFormUrlEncodedBody(
        "overThresholdRadio" -> "true",
        "overThreshold.month" -> "1",
        "overThreshold.year" -> "2017"
      )) {
        _ redirectsTo controllers.routes.ThresholdController.expectationOverShow().url
      }
    }

    "return 303 with valid data - no selected" in {
      mockSaveThreshold(Future.successful(validThresholdPostIncorp))

      submitAuthorised(testThresholdController.goneOverSubmit(), fakeRequest.withFormUrlEncodedBody(
        "overThresholdRadio" -> "false"
      )) {
        _ redirectsTo controllers.routes.ThresholdController.expectationOverShow().url
      }
    }

  }
  s"GET ${routes.ThresholdController.expectationOverShow()}" should {
    val thresholdexpectOverThresholdTrue = ExpectationOverThresholdView(true, Some(LocalDate.of(2017, 6, 30)))
    val thresholdexpectOverThresholdFalse = ExpectationOverThresholdView(false, None)

    "returnException if no IncorporationInfo Date present" in {
      mockGetThresholdViewModel[ExpectationOverThresholdView](Future.successful(Some(thresholdexpectOverThresholdTrue)))
      val res = intercept[Exception](callAuthorised(testThresholdControllerWithoutIncorpDate.expectationOverShow)
      (a => status(a) shouldBe 500)).getMessage
      res shouldBe expectedError
    }

    "return 200 and the page is NOT prepopulated if there is no view data" in {
      mockGetThresholdViewModel[ExpectationOverThresholdView](Future.successful(None))

      callAuthorised(testThresholdController.expectationOverShow()) {
        _ passJsoupTest { doc =>
          doc.getElementById("expectationOverThreshold.day").attr("value") shouldBe ""
          doc.getElementById("expectationOverThreshold.month").attr("value") shouldBe ""
          doc.getElementById("expectationOverThreshold.year").attr("value") shouldBe ""
          doc.getElementById("expectationOverThresholdRadio-true").attr("checked") shouldBe ""
        }
      }
    }

    "return 200 and elements are populated when there's a over threshold view with date" in {
      mockGetThresholdViewModel[ExpectationOverThresholdView](Future.successful(Some(thresholdexpectOverThresholdTrue)))

      callAuthorised(testThresholdController.expectationOverShow()) {
        _ passJsoupTest { doc =>
          doc.getElementById("expectationOverThreshold.day").attr("value") shouldBe "30"
          doc.getElementById("expectationOverThreshold.month").attr("value") shouldBe "6"
          doc.getElementById("expectationOverThreshold.year").attr("value") shouldBe "2017"
          doc.getElementById("expectationOverThresholdRadio-true").attr("checked") shouldBe "checked"
        }
      }
    }

    "return 200 and elements are populated when there's a over threshold view with no date" in {
      mockGetThresholdViewModel[ExpectationOverThresholdView](Future.successful(Some(thresholdexpectOverThresholdFalse)))

      callAuthorised(testThresholdController.expectationOverShow()) {
        _ passJsoupTest { doc =>
          doc.getElementById("expectationOverThreshold.day").attr("value") shouldBe ""
          doc.getElementById("expectationOverThreshold.month").attr("value") shouldBe ""
          doc.getElementById("expectationOverThreshold.year").attr("value") shouldBe ""
          doc.getElementById("expectationOverThresholdRadio-true").attr("checked") shouldBe ""
          doc.getElementById("expectationOverThresholdRadio-false").attr("checked") shouldBe "checked"
        }
      }
    }
  }
  s"POST ${routes.ThresholdController.expectationOverSubmit()}" should {
    "return Exception When Incorporation date is empty" in {

      val res = intercept[Exception](submitAuthorised
      (testThresholdControllerWithoutIncorpDate.expectationOverSubmit(), fakeRequest.withFormUrlEncodedBody())
      (a => status(a) shouldBe 500)).getMessage
      res shouldBe expectedError
    }

    "return 400 when no data posted" in {

      submitAuthorised(
        testThresholdController.expectationOverSubmit(), fakeRequest.withFormUrlEncodedBody()) {
        status(_) shouldBe Status.BAD_REQUEST
      }
    }

    "return 303 with valid data - yes selected" in {
      val thresholdexpectOverThresholdTrue = ExpectationOverThresholdView(true, Some(LocalDate.of(2017, 1, 1)))

      mockGetThresholdViewModel[ExpectationOverThresholdView](Future.successful(Some(thresholdexpectOverThresholdTrue)))

      submitAuthorised(testThresholdController.expectationOverSubmit(), fakeRequest.withFormUrlEncodedBody(
        "expectationOverThresholdRadio" -> "true",
        "expectationOverThreshold.day" -> "01",
        "expectationOverThreshold.month" -> "01",
        "expectationOverThreshold.year" -> "2017"
      )) {
        _ redirectsTo controllers.routes.ThresholdSummaryController.show().url
      }
    }

  }
}
