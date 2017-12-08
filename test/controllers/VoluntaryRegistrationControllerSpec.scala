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
import mocks.ThresholdServiceMock
import models.CurrentProfile
import models.view.VoluntaryRegistration
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class VoluntaryRegistrationControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LFixture {

  class Setup {
    val testController = new VoluntaryRegistrationController {
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

  val fakeRequest = FakeRequest(routes.VoluntaryRegistrationController.show())

  s"GET ${routes.VoluntaryRegistrationController.show()}" should {
    val expectedText = "Do you want to register voluntarily for VAT?"

    "return 200 with HTML not prepopulated when there is no view data" in new Setup {
      mockGetThresholdViewModel[VoluntaryRegistration](Future.successful(None))

      callAuthorised(testController.show()) { res =>
        res includesText expectedText
        res passJsoupTest { doc =>
          doc.getElementById("voluntaryRegistrationRadio-register_no").attr("checked") shouldBe ""
          doc.getElementById("voluntaryRegistrationRadio-register_yes").attr("checked") shouldBe ""
        }
      }
    }

    "return 200 with HTML prepopulated to YES when there is view data" in new Setup {
      mockGetThresholdViewModel[VoluntaryRegistration](Future.successful(Some(validVoluntaryRegistrationView.copy(yesNo = VoluntaryRegistration.REGISTER_YES))))

      callAuthorised(testController.show) {
        _ passJsoupTest { doc =>
          doc.getElementById("voluntaryRegistrationRadio-register_no").attr("checked") shouldBe ""
          doc.getElementById("voluntaryRegistrationRadio-register_yes").attr("checked") shouldBe "checked"
        }
      }
    }

    "return 200 with HTML prepopulated to NO when there is view data" in new Setup {
      mockGetThresholdViewModel[VoluntaryRegistration](Future.successful(Some(validVoluntaryRegistrationView.copy(yesNo = VoluntaryRegistration.REGISTER_NO))))

      callAuthorised(testController.show) {
        _ passJsoupTest { doc =>
          doc.getElementById("voluntaryRegistrationRadio-register_no").attr("checked") shouldBe "checked"
          doc.getElementById("voluntaryRegistrationRadio-register_yes").attr("checked") shouldBe ""
        }
      }
    }
  }

  s"POST ${routes.VoluntaryRegistrationController.submit()} with Empty data" should {
    "return 400" in new Setup {
      submitAuthorised(testController.submit(), fakeRequest.withFormUrlEncodedBody()) {
        result => result isA 400
      }
    }
  }

  s"POST ${routes.VoluntaryRegistrationController.submit()} with Voluntary Registration selected Yes" should {
    "return 303" in new Setup {
      mockSaveThreshold(Future.successful(validThresholdPreIncorp))
      submitAuthorised(testController.submit(), fakeRequest.withFormUrlEncodedBody(
        "voluntaryRegistrationRadio" -> VoluntaryRegistration.REGISTER_YES
      ))(_ redirectsTo controllers.routes.VoluntaryRegistrationReasonController.show().url)
    }
  }

  s"POST ${routes.VoluntaryRegistrationController.submit()} with Voluntary Registration selected No" should {
    "redirect to the welcome page" in new Setup {
      mockSaveThreshold(Future.successful(validThresholdPreIncorp.copy(voluntaryRegistration = Some(VoluntaryRegistration(VoluntaryRegistration.REGISTER_NO)))))

      when(mockVatRegFrontendService.buildVatRegFrontendUrlWelcome)
        .thenReturn(s"someUrl")

      submitAuthorised(testController.submit(), fakeRequest.withFormUrlEncodedBody(
        "voluntaryRegistrationRadio" -> VoluntaryRegistration.REGISTER_NO
      ))(_ redirectsTo "someUrl")
    }
  }
}
