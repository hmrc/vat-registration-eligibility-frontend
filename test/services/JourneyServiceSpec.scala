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

package services

import base.SpecBase
import models.CurrentProfile
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.{ExecutionContext, Future}

class JourneyServiceSpec extends SpecBase {

  class Setup {
    val service = new JourneyService(mockSessionService, mockVatRegConnector)
  }

  val testRegId = "testRegId"
  val testDifferentRegId = "testDifferentRegId"
  val testSessionId = "testSessionId"
  val testCacheMap: CacheMap = CacheMap(testSessionId, Map("CurrentProfile" -> Json.toJson(CurrentProfile(testRegId))))
  val testDifferentCacheMap: CacheMap = CacheMap(testSessionId, Map("CurrentProfile" -> Json.toJson(CurrentProfile(testDifferentRegId))))

  "initialiseJourney" when {
    "the session contains user data with a matching regId" must {
      "allow the user to continue with their current session unchanged" in new Setup {
        when(mockSessionService.fetch)
          .thenReturn(Future.successful(Some(testCacheMap)))

        val res: CacheMap = await(service.initialiseJourney(testRegId))

        res mustBe testCacheMap
      }
    }
    "the session doesn't contain user data with a matching regId" when {
      "the backend has user data for this regId" must {
        "store in session and return the user data from backend" in new Setup {
          when(mockSessionService.fetch)
            .thenReturn(Future.successful(Some(testDifferentCacheMap)))

          when(mockVatRegConnector.getEligibilityAnswers(ArgumentMatchers.eq(testRegId))(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.successful(Some(testCacheMap.data)))

          when(mockSessionService.sessionId(any[HeaderCarrier]))
            .thenReturn(testSessionId)

          when(mockSessionService.save(ArgumentMatchers.eq(testCacheMap)))
            .thenReturn(Future.successful(testCacheMap))

          val res: CacheMap = await(service.initialiseJourney(testRegId))

          res mustBe testCacheMap
        }
      }
      "the backend doesn't have user data for this regId" must {
        "store in session and return an empty cachemap" in new Setup {
          when(mockSessionService.fetch)
            .thenReturn(Future.successful(None))

          when(mockVatRegConnector.getEligibilityAnswers(ArgumentMatchers.eq(testRegId))(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(Future.successful(None))

          when(mockSessionService.sessionId(any[HeaderCarrier]))
            .thenReturn(testSessionId)

          when(mockSessionService.save(ArgumentMatchers.eq(testCacheMap)))
            .thenReturn(Future.successful(testCacheMap))

          val res: CacheMap = await(service.initialiseJourney(testRegId))

          res mustBe testCacheMap
        }
      }
    }
  }

}
