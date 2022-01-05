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

package services

import base.CommonSpecBase
import mocks.S4LServiceMock
import models.CurrentProfile
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CurrentProfileServiceSpec extends CommonSpecBase with S4LServiceMock {

  class Setup {
    val service = new CurrentProfileService(mockSessionService, mockVatRegConnector, mockS4LService)
  }

  val testIntId = "internalId"

  "buildCurrentProfile" should {
    "build a profile" when {
      "it hasn't been built" in new Setup {
        when(mockSessionService.getEntry[CurrentProfile](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(None))
        when(mockVatRegConnector.getRegistrationId()(ArgumentMatchers.any[HeaderCarrier], ArgumentMatchers.any[ExecutionContext]))
          .thenReturn(Future.successful(regId))
        when(mockSessionService.save[CurrentProfile](ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(CacheMap("test", Map("test" -> Json.obj()))))
        mockS4LFetchAndGet(regId, "eligibility-data")(None)

        await(service.fetchOrBuildCurrentProfile(testIntId)) mustBe CurrentProfile(regId)
      }

      "it has been built with S4L" in new Setup {
        private val testCacheMap = CacheMap(regId, Map("CurrentProfile" -> Json.toJson(CurrentProfile(regId))))

        when(mockSessionService.getEntry[CurrentProfile](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(None))
        when(mockVatRegConnector.getRegistrationId()(ArgumentMatchers.any[HeaderCarrier], ArgumentMatchers.any[ExecutionContext]))
          .thenReturn(Future.successful(regId))
        when(mockSessionService.save(ArgumentMatchers.eq(testCacheMap)))
          .thenReturn(Future.successful(testCacheMap))
        mockS4LFetchAndGet(regId, "eligibility-data")(Some(testCacheMap))

        await(service.fetchOrBuildCurrentProfile(testIntId)) mustBe CurrentProfile(regId)
      }

      "it has been built" in new Setup {
        private val profile = CurrentProfile(regId)

        when(mockSessionService.getEntry[CurrentProfile](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(profile)))

        await(service.fetchOrBuildCurrentProfile(testIntId)) mustBe profile
      }
    }
  }
}
