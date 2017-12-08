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

import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.CurrentProfile
import models.external.IncorporationInfo
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class EligibilitySummaryControllerSpec extends VatRegSpec with VatRegistrationFixture {

  class Setup {

    val testController = new EligibilitySummaryController {
      override val authConnector = mockAuthConnector
      override val summaryService = mockSummaryService
      override val vatRegistrationService = mockVatRegistrationService
      override val currentProfileService = mockCurrentProfileService
      override val messagesApi = mockMessages

      override def withCurrentProfile(f: (CurrentProfile) => Future[Result])(implicit request: Request[_], hc: HeaderCarrier): Future[Result] = {
        f(currentProfile)
      }
    }

    val INCORPORATION_STATUS = "incorporationStatus"
  }

  val fakeRequest = FakeRequest(controllers.routes.EligibilitySummaryController.show())

  "Calling eligibility summary to show the Eligibility summary page" should {
    "return HTML with a valid threshold summary view" in new Setup {
      when(mockSummaryService.getEligibilitySummary(ArgumentMatchers.any(),ArgumentMatchers.any()))
        .thenReturn(validEligibilitySummary)

      callAuthorised(testController.show)(_ includesText "Check and confirm your answers")
    }
  }

  s"POST ${controllers.routes.EligibilitySummaryController.submit()}" should {
    "return 303 with valid data - Company NOT INCORPORATED" in new Setup {
      when(mockVatRegistrationService.getIncorporationDate(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(None))

      mockKeystoreFetchAndGet[IncorporationInfo](INCORPORATION_STATUS, None)

      submitAuthorised(testController.submit(), fakeRequest.withFormUrlEncodedBody()) {
        _ redirectsTo controllers.routes.TaxableTurnoverController.show().url
      }
    }
  }
}
