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

package services

import java.time.LocalDate

import common.enums.VatRegStatus
import connectors.BusinessRegistrationConnector
import fixtures.VatRegistrationFixture
import helpers.FutureAssertions
import mocks.VatMocks
import models.CurrentProfile
import models.external.{BusinessProfile, CompanyRegistrationProfile}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Format
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class CurrentProfileServiceSpec extends UnitSpec with MockitoSugar with VatMocks with FutureAssertions with VatRegistrationFixture {
  val mockBusinessRegistrationConnector = mock[BusinessRegistrationConnector]

  val testService = new CurrentProfileService {
    override val keystoreConnector = mockKeystoreConnector
    override val businessRegistrationConnector = mockBusinessRegistrationConnector
    override val compRegConnector = mockCompanyRegConnector
    override val incorpInfoService = mockIncorpInfoService
    override val vatRegistrationService = mockVatRegistrationService
  }

  val now = LocalDate.now()

  val testCompanyName = "testCompanyName"
  val regId = "12345"
  val txId = "000-12345"

  def testCurrentProfile(incorpDate: Option[LocalDate] = Some(now)) = CurrentProfile(
    companyName           = testCompanyName,
    registrationId        = regId,
    transactionId         = txId,
    vatRegistrationStatus = VatRegStatus.draft,
    incorporationDate     = incorpDate
  )

  "getCurrentProfile" should {
    implicit val hc = HeaderCarrier()

    "return a CurrentProfile" when {
      "fetched from Keystore" in {
        when(mockKeystoreConnector.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any[HeaderCarrier](), ArgumentMatchers.any[Format[CurrentProfile]]()))
          .thenReturn(Future.successful(Some(testCurrentProfile())))

        val result = await(testService.getCurrentProfile())
        result shouldBe testCurrentProfile()
      }

      "build and store in Keystore" in {
        val businessProfile = BusinessProfile(regId, "EN")
        val compRegDetails = CompanyRegistrationProfile("accepted", txId)

        when(mockKeystoreConnector.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any[HeaderCarrier](), ArgumentMatchers.any[Format[CurrentProfile]]()))
          .thenReturn(Future.successful(None))

        when(mockBusinessRegistrationConnector.retrieveBusinessProfile(ArgumentMatchers.any[HeaderCarrier](), ArgumentMatchers.any[HttpReads[BusinessProfile]]))
          .thenReturn(Future.successful(businessProfile))

        when(mockCompanyRegConnector.getCompanyRegistrationDetails(ArgumentMatchers.any())(ArgumentMatchers.any[HeaderCarrier]()))
          .thenReturn(Future.successful(compRegDetails))

        when(mockIncorpInfoService.getCompanyName(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any[HeaderCarrier]()))
          .thenReturn(Future.successful(testCompanyName))

        when(mockVatRegistrationService.getStatus(ArgumentMatchers.any())(ArgumentMatchers.any[HeaderCarrier]()))
          .thenReturn(Future.successful(VatRegStatus.draft))

        when(mockVatRegistrationService.getIncorporationInfo(ArgumentMatchers.any())(ArgumentMatchers.any[HeaderCarrier]()))
          .thenReturn(Future.successful(Some(testIncorporationInfo)))

        when(mockKeystoreConnector.cache[CurrentProfile](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(CacheMap("", Map())))

        val result = await(testService.getCurrentProfile())
        result shouldBe testCurrentProfile(testIncorporationInfo.statusEvent.incorporationDate)
      }
    }
  }
}
