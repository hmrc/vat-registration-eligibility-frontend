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

package mocks

import connectors.{CompanyRegistrationConnector, IncorporationInformationConnector, VatRegistrationConnector}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.RequestHeader
import services._
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.audit.model.Audit
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

trait VatMocks extends SaveForLaterMock with KeystoreMock with WSHTTPMock with ThresholdServiceMock
               with EligibilityServiceMock with VatRegConnectorMock { this: MockitoSugar =>

  implicit lazy val mockMessagesApi             = mock[MessagesApi]
  implicit lazy val mockAuthConnector           = mock[AuthConnector]
  implicit lazy val mockSessionCache            = mock[SessionCache]
  implicit lazy val mockAudit                   = mock[Audit]
  implicit lazy val mockCurrentProfileService   = mock[CurrentProfileService]
  implicit lazy val mockCompanyRegConnector     = mock[CompanyRegistrationConnector]
  implicit lazy val mockIIConnector             = mock[IncorporationInformationConnector]
  implicit lazy val mockVatRegistrationService  = mock[VatRegistrationService]
  implicit lazy val mockIncorpInfoService       = mock[IncorporationInformationService]
  implicit lazy val mockVatRegFrontendService   = mock[VatRegFrontendService]
  implicit lazy val mockSummaryService          = mock[SummaryService]
  implicit lazy val mockCancellationService     = mock[CancellationService]
  
  val lang = Lang("en")
  implicit val messages = Messages(lang, mockMessagesApi)

  val MOCKED_MESSAGE = "mocked message"


  def mockAllMessages: OngoingStubbing[String] = {
    when(mockMessagesApi.preferred(any[RequestHeader]()))
      .thenReturn(messages)

    when(mockMessagesApi.apply(any[String](), any())(any()))
      .thenReturn(MOCKED_MESSAGE)
  }
}
