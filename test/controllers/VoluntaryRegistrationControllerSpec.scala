/*
 * Copyright 2018 HM Revenue & Customs
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

import connectors.S4LConnector
import fixtures.VatRegistrationFixture
import helpers.{ControllerSpec, FutureAssertions}
import models.CurrentProfile
import models.external.IncorporationInfo
import models.view.VoluntaryRegistration
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.i18n.MessagesApi
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.{HeaderCarrier, Upstream5xxResponse}

import scala.concurrent.Future

class VoluntaryRegistrationControllerSpec extends ControllerSpec with GuiceOneAppPerTest with VatRegistrationFixture with FutureAssertions {

  class Setup {
    val testController = new VoluntaryRegistrationController {
      override val authConnector: AuthConnector = mockAuthClientConnector
      override val thresholdService = mockThresholdService
      override val vatRegFrontendService = mockVatRegFrontendService
      override val currentProfileService = mockCurrentProfileService
      override val compRegFEURL: String = "testUrl"
      override val compRegFEURI: String = "/testUri"
      override val compRegFECompanyRegistrationOverview: String = "/testcompany-registration-overview"
      override val s4LConnector: S4LConnector = mockS4LConnector
      override val messagesApi = fakeApplication.injector.instanceOf(classOf[MessagesApi])

      override def withCurrentProfile(f: (CurrentProfile) => Future[Result])(implicit request: Request[_], hc: HeaderCarrier): Future[Result] = {
        f(currentProfile)
      }
    }
  }

  val fakeRequest = FakeRequest(routes.VoluntaryRegistrationController.show())

  s"GET ${routes.VoluntaryRegistrationController.show()}" should {
    val expectedText = "You can still choose to register it voluntarily. If you do, the company may be able to reclaim VAT on business-related purchases."

    "return 200 with HTML not prepopulated when there is no view data" in new Setup {
      mockGetThresholdViewModel[VoluntaryRegistration](Future.successful(None))

      callAuthenticated(testController.show()) { res =>
        res includesText expectedText
        res passJsoupTest { doc =>
          doc.getElementById("voluntaryRegistrationRadio-register_no").attr("checked") mustBe ""
          doc.getElementById("voluntaryRegistrationRadio-register_yes").attr("checked") mustBe ""
        }
      }
    }

    "return 200 with HTML prepopulated to YES when there is view data" in new Setup {
      mockGetThresholdViewModel[VoluntaryRegistration](Future.successful(Some(validVoluntaryRegistrationView.copy(yesNo = VoluntaryRegistration.REGISTER_YES))))

      callAuthenticated(testController.show) {
        _ passJsoupTest { doc =>
          doc.getElementById("voluntaryRegistrationRadio-register_no").attr("checked") mustBe ""
          doc.getElementById("voluntaryRegistrationRadio-register_yes").attr("checked") mustBe "checked"
        }
      }
    }

    "return 200 with HTML prepopulated to NO when there is view data" in new Setup {
      mockGetThresholdViewModel[VoluntaryRegistration](Future.successful(Some(validVoluntaryRegistrationView.copy(yesNo = VoluntaryRegistration.REGISTER_NO))))

      callAuthenticated(testController.show) {
        _ passJsoupTest { doc =>
          doc.getElementById("voluntaryRegistrationRadio-register_no").attr("checked") mustBe "checked"
          doc.getElementById("voluntaryRegistrationRadio-register_yes").attr("checked") mustBe ""
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

  "return 200 with HTML when user selects no to voluntary registration" in new Setup {
    callAuthenticated(testController.showChoseNoToVoluntary) {
      res => status(res) mustBe 200
        res passJsoupTest { doc =>
          doc.getElementById("confirm-and-continue").attr("id") mustBe "confirm-and-continue"
        }
    }
  }

  s"POST ${routes.VoluntaryRegistrationController.submit()} with Voluntary Registration selected No" should {
    "redirect to the dashboard page" in new Setup {
      mockSaveThreshold(Future.successful(validThresholdPreIncorp.copy(voluntaryRegistration = Some(VoluntaryRegistration(VoluntaryRegistration.REGISTER_NO)))))
      submitAuthorised(testController.submit(), fakeRequest.withFormUrlEncodedBody(
        "voluntaryRegistrationRadio" -> VoluntaryRegistration.REGISTER_NO
      ))(_ redirectsTo controllers.routes.VoluntaryRegistrationController.showChoseNoToVoluntary().url)
    }
  }

  s"GET ${controllers.routes.VoluntaryRegistrationController.showClearS4lRedirectDashboard()}" should {
    "return 303 with clear s4l and redirect to dashboard" in new Setup {
      mockS4LClear()
      submitAuthorised(testController.showClearS4lRedirectDashboard(), fakeRequest.withFormUrlEncodedBody()) {
        (_ redirectsTo (s"${testController.compRegFEURL}${testController.compRegFEURI}${testController.compRegFECompanyRegistrationOverview}"))
      }
    }
  }

  s"GET ${routes.VoluntaryRegistrationController.showClearS4lRedirectDashboard()} when S4l returns an exception" should {
    "return exception" in new Setup {
      when(mockS4LConnector.clear(ArgumentMatchers.anyString())(ArgumentMatchers.any[HeaderCarrier]())).thenReturn(Future.failed(new Upstream5xxResponse("Forbidden", 500, 500)))
      submitAuthorised(testController.showClearS4lRedirectDashboard(), fakeRequest.withFormUrlEncodedBody(
      ))(result => intercept[Upstream5xxResponse](await(result)) mustBe  Upstream5xxResponse("Forbidden", 500, 500))
    }
  }
}
