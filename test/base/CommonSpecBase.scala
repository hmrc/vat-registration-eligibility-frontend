/*
 * Copyright 2023 HM Revenue & Customs
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

import org.mockito.Mockito.reset
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

trait CommonSpecBase extends PlaySpec with FutureAwaits with DefaultAwaitTimeout with BeforeAndAfterEach with MockitoSugar with VATEligibilityMocks {
  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Option(SessionId("sess-ID")))
  val regId = "regId"
  val txId = "txId"

  override def beforeEach() = {
    super.beforeEach()
    resetMocks()
  }

  def resetMocks() = {
    reset(
      mockSessionService,
      mockVatRegConnector,
      mockAuthConnector,
      mockVRService,
      mockJourneyService,
      mockDataRequiredAction,
      mockMessagesAPI,
      mockAuditConnector
    )
  }
}
