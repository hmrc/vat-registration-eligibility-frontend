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

import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.CurrentProfile
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class CancellationServiceSpec extends VatRegSpec with VatRegistrationFixture {

  class Setup {
    val service = new CancellationService {
      override val keystoreConnector = mockKeystoreConnector
      override val s4LConnector = mockS4LConnector
      override val currentProfileService = mockCurrentProfileService
    }

    def mockBuildCurrentProfile(regId: String) = when(mockCurrentProfileService.buildCurrentProfile(ArgumentMatchers.any()))
      .thenReturn(Future.successful(currentProfile.copy(registrationId = regId)))

    def fetchProfileFromKeystore(regId: String, result: Boolean) = {
      val profile = if(result) Some(currentProfile.copy(registrationId = regId)) else None
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(profile))
    }

    def removeFromKeystore = when(mockKeystoreConnector.remove()(ArgumentMatchers.any()))
      .thenReturn(Future.successful(HttpResponse(200)))

    def failedRemoveFromKeystore = when(mockKeystoreConnector.remove()(ArgumentMatchers.any()))
      .thenReturn(Future.failed(new Exception("ThrownError")))

    def clearFromS4Later = when(mockS4LConnector.clear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(HttpResponse(200)))

    def failedClearFromS4Later = when(mockS4LConnector.clear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.failed(new Exception("ThrownError")))
  }

  "deleteEligibilityData" should {
    "return true if the delete is succesful" in new Setup {
      val regId = "SuccessId"
      fetchProfileFromKeystore(regId, true)
      removeFromKeystore
      clearFromS4Later
      await(service.deleteEligibilityData(regId)) shouldBe true
    }

    "return false if the regId doesnt match the current profile ID" in new Setup {
      val regId = "FailureId"
      fetchProfileFromKeystore("DoesntMatch", true)
      await(service.deleteEligibilityData(regId)) shouldBe false
    }

    "build current profile and return true if regId match" in new Setup {
      val regId = "SuccessId"
      fetchProfileFromKeystore(regId, false)
      mockBuildCurrentProfile(regId)
      removeFromKeystore
      clearFromS4Later
      await(service.deleteEligibilityData(regId)) shouldBe true
    }

    "build current profile and return false if regId dont match" in new Setup {
      val regId = "FailureId"
      fetchProfileFromKeystore(regId, false)
      mockBuildCurrentProfile("DoesntMatch")
      removeFromKeystore
      clearFromS4Later
      await(service.deleteEligibilityData(regId)) shouldBe false
    }

    "throw an error if keystore remove failed with an error" in new Setup {
      val regId = "SuccessId"
      fetchProfileFromKeystore(regId, true)
      mockBuildCurrentProfile(regId)
      failedRemoveFromKeystore
      intercept[Exception](await(service.deleteEligibilityData(regId))).getMessage shouldBe "ThrownError"
    }

    "throw an error if s4l clear failed with an error" in new Setup {
      val regId = "SuccessId"
      fetchProfileFromKeystore(regId, true)
      mockBuildCurrentProfile(regId)
      removeFromKeystore
      failedClearFromS4Later
      intercept[Exception](await(service.deleteEligibilityData(regId))).getMessage shouldBe "ThrownError"
    }
  }
}
