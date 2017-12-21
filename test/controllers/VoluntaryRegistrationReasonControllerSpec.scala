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

import fixtures.{S4LFixture, VatRegistrationFixture}
import helpers.VatRegSpec
import models.CurrentProfile
import models.view.VoluntaryRegistrationReason
import models.view.VoluntaryRegistrationReason._
import org.mockito.Mockito._
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class VoluntaryRegistrationReasonControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LFixture {

  class Setup {
    val testController = new VoluntaryRegistrationReasonController {
      override val authConnector: AuthConnector = mockAuthConnector
      override val thresholdService = mockThresholdService
      override val vatRegFrontendService = mockVatRegFrontendService
      override val currentProfileService = mockCurrentProfileService
      override val messagesApi = mockMessages

      override def withCurrentProfile(f: (CurrentProfile) => Future[Result])(implicit request: Request[_], hc: HeaderCarrier): Future[Result] = {
        f(currentProfile)
      }
    }
  }

  val fakeRequest = FakeRequest(routes.VoluntaryRegistrationReasonController.show())

  s"GET ${routes.VoluntaryRegistrationReasonController.show()}" should {
    val expectedText = "Which one applies to the company?"
    "return 200 with HTML not prepopulated when there is no view data" in new Setup{
        mockGetThresholdViewModel[VoluntaryRegistrationReason](Future.successful(None))

      callAuthorised(testController.show()){ res =>
        res includesText expectedText
        res passJsoupTest { doc =>
          doc.getElementsByAttribute("checked").size shouldBe 0
        }
      }
    }

    "return 200 with HTML prepopulated with SELLS when there is view data" in new Setup {
      mockGetThresholdViewModel[VoluntaryRegistrationReason](Future.successful(Some(validVoluntaryRegistrationReasonView.copy(reason = SELLS))))

      callAuthorised(testController.show) {
        _ passJsoupTest { doc =>
          val elements = doc.getElementsByAttribute("checked")
          elements.size shouldBe 1
          elements.first.attr("id") shouldBe "voluntaryRegistrationReasonRadio-alreadysellsvattaxablegoodsorservices"
        }
      }
    }

    "return 200 with HTML prepopulated with INTENDS when there is view data" in new Setup {
      mockGetThresholdViewModel[VoluntaryRegistrationReason](Future.successful(Some(validVoluntaryRegistrationReasonView.copy(reason = INTENDS_TO_SELL))))

      callAuthorised(testController.show) {
        _ passJsoupTest { doc =>
          val elements = doc.getElementsByAttribute("checked")
          elements.size shouldBe 1
          elements.first.attr("id") shouldBe "voluntaryRegistrationReasonRadio-intendstosellvattaxablegoodsorservices"
        }
      }
    }

    "return 200 with HTML prepopulated with NEITHER when there is view data" in new Setup {
      mockGetThresholdViewModel[VoluntaryRegistrationReason](Future.successful(Some(validVoluntaryRegistrationReasonView.copy(reason = NEITHER))))

      callAuthorised(testController.show) {
        _ passJsoupTest { doc =>
          val elements = doc.getElementsByAttribute("checked")
          elements.size shouldBe 1
          elements.first.attr("id") shouldBe "voluntaryRegistrationReasonRadio-wontsellvattaxablegoodsorservices"
        }
      }
    }
  }

  s"POST ${routes.VoluntaryRegistrationReasonController.submit()} with Empty data" should {
    "return 400" in new Setup {
      submitAuthorised(testController.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }
  }

  s"POST ${routes.VoluntaryRegistrationReasonController.submit()} with Voluntary Registration Reason selected Sells" should {
    "return 303" in new Setup {
      mockSaveThreshold(Future.successful(validThresholdPreIncorp))

      when(mockVatRegFrontendService.buildVatRegFrontendUrlEntry)
        .thenReturn(s"someUrl")

      submitAuthorised(testController.submit(), fakeRequest.withFormUrlEncodedBody(
        "voluntaryRegistrationReasonRadio" -> VoluntaryRegistrationReason.SELLS
      ))(_ redirectsTo s"someUrl")
    }
  }

  s"POST ${routes.VoluntaryRegistrationReasonController.submit()} with Voluntary Registration Reason selected Intends to sell" should {
    "return 303" in new Setup {
      val validOtherThresholdPreIncorp = validThresholdPreIncorp.copy(
        voluntaryRegistrationReason = Some(VoluntaryRegistrationReason(VoluntaryRegistrationReason.INTENDS_TO_SELL))
      )

      mockSaveThreshold(Future.successful(validOtherThresholdPreIncorp))

      when(mockVatRegFrontendService.buildVatRegFrontendUrlEntry)
        .thenReturn(s"someUrl")

      submitAuthorised(testController.submit(), fakeRequest.withFormUrlEncodedBody(
        "voluntaryRegistrationReasonRadio" -> VoluntaryRegistrationReason.INTENDS_TO_SELL
      )){_ redirectsTo s"someUrl"}
    }
  }

  s"POST ${routes.VoluntaryRegistrationReasonController.submit()} with Voluntary Registration selected Neither" should {
    "redirect to the welcome page" in new Setup {
      val validOtherThresholdPreIncorp = validThresholdPreIncorp.copy(
        voluntaryRegistrationReason = Some(VoluntaryRegistrationReason(VoluntaryRegistrationReason.NEITHER))
      )

      mockSaveThreshold(Future.successful(validOtherThresholdPreIncorp))

      when(mockVatRegFrontendService.buildVatRegFrontendUrlWelcome)
        .thenReturn(s"someUrl")

      submitAuthorised(testController.submit(), fakeRequest.withFormUrlEncodedBody(
        "voluntaryRegistrationReasonRadio" -> VoluntaryRegistrationReason.NEITHER
      ))(_ redirectsTo "someUrl")
    }
  }
}
