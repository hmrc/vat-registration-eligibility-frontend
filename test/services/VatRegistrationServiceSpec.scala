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

import cats.data.OptionT
import common.enums.CacheKeys
import fixtures.{S4LFixture, VatRegistrationFixture}
import helpers.VatRegSpec
import models._
import models.external.IncorporationInfo
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future
import scala.language.postfixOps

class VatRegistrationServiceSpec extends VatRegSpec with VatRegistrationFixture with S4LFixture {

  class Setup {
    val service = new VatRegistrationService {
      override val vatRegConnector = mockRegConnector
      override val keystoreConnector = mockKeystoreConnector
    }
  }

  override def beforeEach() {
    super.beforeEach()
    mockFetchRegId(testRegId)
    when(mockRegConnector.getIncorporationInfo(any())(any()))
      .thenReturn(Future.successful(None))
  }

  "Calling getIncorporationInfo" should {
    "successfully returns an incorporation information" in new Setup {
      when(mockRegConnector.getIncorporationInfo(ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(testIncorporationInfo)))

      service.getIncorporationInfo("txId") returns Some(testIncorporationInfo)
    }
  }

  "Calling getIncorporationDate" should {
    "successfully returns an incorporation date from keystore" in new Setup {
      mockKeystoreFetchAndGet[CurrentProfile](CacheKeys.CurrentProfile.toString, Some(currentProfile))

      service.getIncorporationDate returns currentProfile.incorporationDate
    }

    "successfully returns an incorporation date from microservice and save to keystore" in new Setup {
      when(mockRegConnector.getIncorporationInfo(ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(testIncorporationInfo)))

      mockKeystoreCache[String](CacheKeys.CurrentProfile.toString, CacheMap("", Map.empty))

      service.getIncorporationDate(currentProfile.copy(incorporationDate = None), hc) returns testIncorporationInfo.statusEvent.incorporationDate
    }
  }
}
