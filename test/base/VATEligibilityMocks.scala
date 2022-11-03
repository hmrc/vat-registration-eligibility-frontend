/*
 * Copyright 2022 HM Revenue & Customs
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

package base

import connectors._
import controllers.actions.DataRequiredAction
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import services._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

trait VATEligibilityMocks {
  self: MockitoSugar =>

  //Connectors
  lazy val mockSessionService = mock[SessionService]
  lazy val mockVatRegConnector = mock[VatRegistrationConnector]
  lazy val mockAuthConnector = mock[AuthConnector]
  lazy val mockAuditConnector = mock[AuditConnector]

  //Services
  lazy val mockVRService = mock[VatRegistrationService]
  lazy val mockJourneyService = mock[JourneyService]

  //Other
  lazy val mockDataRequiredAction = mock[DataRequiredAction]
  lazy val mockMessagesAPI = mock[MessagesApi]
}
