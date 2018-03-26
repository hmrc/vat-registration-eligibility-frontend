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

import fixtures.VatRegistrationFixture
import helpers.{ControllerSpec, FutureAssertions}
import models.CurrentProfile
import models.view.TaxableTurnover
import models.view.TaxableTurnover._
import org.mockito.Mockito._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.i18n.MessagesApi
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class TaxableTurnoverControllerSpec extends ControllerSpec with GuiceOneAppPerTest with VatRegistrationFixture with FutureAssertions {

  class Setup {
    val testController = new TaxableTurnoverController {
      override val authConnector: AuthConnector = mockAuthClientConnector
      override val thresholdService = mockThresholdService
      override val vatRegFrontendService = mockVatRegFrontendService
      override val currentProfileService = mockCurrentProfileService
      override val messagesApi = fakeApplication.injector.instanceOf(classOf[MessagesApi])

      override def withCurrentProfile(f: (CurrentProfile) => Future[Result])(implicit request: Request[_], hc: HeaderCarrier): Future[Result] = {
        f(currentProfile)
      }
    }
  }

  val fakeRequest = FakeRequest(routes.TaxableTurnoverController.show())

  s"GET ${routes.TaxableTurnoverController.show()}" should {
    val expectedText = "Over the next 30 days, do you think the company will make more than £85,000 in VAT-taxable sales?"

    "return 200 with HTML not prepopulated when there is no view data" in new Setup {
      mockGetThresholdViewModel[TaxableTurnover](Future.successful(None))

      callAuthenticated(testController.show) { res =>
        res includesText expectedText
        res passJsoupTest { doc =>
          doc.getElementById("taxableTurnoverRadio-taxable_yes").attr("checked") mustBe ""
          doc.getElementById("taxableTurnoverRadio-taxable_no").attr("checked") mustBe ""
        }
      }
    }

    "return 200 with HTML prepopulated to YES when there is view data" in new Setup {
      mockGetThresholdViewModel[TaxableTurnover](Future.successful(Some(TaxableTurnover(TAXABLE_YES))))

      callAuthenticated(testController.show) {
        _ passJsoupTest { doc =>
          doc.getElementById("taxableTurnoverRadio-taxable_yes").attr("checked") mustBe "checked"
          doc.getElementById("taxableTurnoverRadio-taxable_no").attr("checked") mustBe ""
        }
      }
    }

    "return 200 with HTML prepopulated to NO when there is view data" in new Setup {
      mockGetThresholdViewModel[TaxableTurnover](Future.successful(Some(TaxableTurnover(TAXABLE_NO))))

      callAuthenticated(testController.show) {
        _ passJsoupTest { doc =>
          doc.getElementById("taxableTurnoverRadio-taxable_yes").attr("checked") mustBe ""
          doc.getElementById("taxableTurnoverRadio-taxable_no").attr("checked") mustBe "checked"
        }
      }
    }
  }

  s"POST ${routes.TaxableTurnoverController.submit()} with Empty data" should {
    "return 400" in new Setup {

      submitAuthorised(testController.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }
  }

  s"POST ${routes.TaxableTurnoverController.submit()} with Taxable Turnover selected Yes" should {

    "return 303" in new Setup {
      mockSaveThreshold(Future.successful(validThresholdPreIncorp.copy(taxableTurnover = Some(TaxableTurnover(TaxableTurnover.TAXABLE_YES)))))
      when(mockVatRegFrontendService.buildVatRegFrontendUrlEntry)
        .thenReturn("someUrl")

      submitAuthorised(testController.submit(), fakeRequest.withFormUrlEncodedBody(
        "taxableTurnoverRadio" -> TaxableTurnover.TAXABLE_YES
      ))(_ redirectsTo "someUrl")

    }
  }

  s"POST ${routes.TaxableTurnoverController.submit()} with Taxable Turnover selected No" should {
    "return 303" in new Setup {
      import models.view.TaxableTurnover.TAXABLE_NO

      mockSaveThreshold(Future.successful(validThresholdPreIncorp))

      mockGetThresholdViewModel[TaxableTurnover](Future.successful(Some(validTaxableTurnOverView.copy(yesNo = TAXABLE_NO))))

      submitAuthorised(testController.submit(), fakeRequest.withFormUrlEncodedBody(
        "taxableTurnoverRadio" -> TaxableTurnover.TAXABLE_NO
      ))(_ redirectsTo routes.VoluntaryRegistrationController.show.url)
    }
  }

}
