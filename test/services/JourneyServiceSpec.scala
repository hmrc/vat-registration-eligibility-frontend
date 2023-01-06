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
import mocks.S4LServiceMock
import models.CurrentProfile
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.libs.json.{Format, JsString, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class JourneyServiceSpec extends SpecBase with S4LServiceMock  {

  class Setup {
    val service = new JourneyService(mockSessionService, mockVatRegConnector, mockS4LService)
  }

  val testRegId = "testRegId"
  val testIntId = "internalId"
  val testSessionId = "testSessionId"
  val profile = CurrentProfile(testRegId)
  val testCacheMap = CacheMap(testIntId, Map("CurrentProfile" -> Json.toJson(profile)))
  private val dataKey = "eligibility-data"

  "initialiseJourney" when {
    "the session contains data stored under the user's Iiternal ID (old journey)" must {
      "store their data under session ID and return the cachemap" in new Setup {
        val sessionCacheMap = testCacheMap.copy(id = testSessionId)

        when(mockSessionService.sessionId(any[HeaderCarrier]))
          .thenReturn(testSessionId)

        when(mockSessionService.fetch(ArgumentMatchers.eq(testIntId))(any[HeaderCarrier])).
          thenReturn(Future.successful(Some(testCacheMap)))

        when(mockSessionService.save(ArgumentMatchers.eq(sessionCacheMap))(any[HeaderCarrier]))
          .thenReturn(Future.successful(sessionCacheMap))

        val res = await(service.initialiseJourney(testIntId, testRegId))

        res mustBe sessionCacheMap
      }
    }
    "the session doesn't contain data against the users internal ID (new journey)" when {
      "save 4 later contains data against the registration ID for the journey" must {
        "store in session and return the found data" in new Setup {
          val s4lCacheMap = testCacheMap.copy(id = testRegId, data = Map("some" -> JsString("thing")))
          val sessionCacheMap = s4lCacheMap.copy(id = testSessionId)

          when(mockSessionService.sessionId(any[HeaderCarrier]))
            .thenReturn(testSessionId)

          when(mockSessionService.fetch(ArgumentMatchers.eq(testIntId))(any[HeaderCarrier])).
            thenReturn(Future.successful(None))

          when(mockS4LService.fetchAndGet[CacheMap](ArgumentMatchers.eq(testRegId), ArgumentMatchers.eq(dataKey))(any[HeaderCarrier], any[Format[CacheMap]]))
            .thenReturn(Future.successful(Some(s4lCacheMap)))

          when(mockSessionService.save(ArgumentMatchers.eq(sessionCacheMap))(any[HeaderCarrier]))
            .thenReturn(Future.successful(sessionCacheMap))

          val res = await(service.initialiseJourney(testIntId, testRegId))

          res mustBe sessionCacheMap
        }
      }
      "save 4 later doesn't contain data against the registration ID for the journey" must {
        "store in session and return an empty cachemap" in new Setup {
          val sessionCacheMap = testCacheMap.copy(id = testSessionId)

          when(mockSessionService.sessionId(any[HeaderCarrier]))
            .thenReturn(testSessionId)

          when(mockJourneyService.emptyCacheMap(ArgumentMatchers.eq(testRegId))(any[HeaderCarrier]))
            .thenReturn(sessionCacheMap)

          when(mockSessionService.fetch(ArgumentMatchers.eq(testIntId))(any[HeaderCarrier])).
            thenReturn(Future.successful(None))

          when(mockS4LService.fetchAndGet[CacheMap](ArgumentMatchers.eq(testRegId), ArgumentMatchers.eq(dataKey))(any[HeaderCarrier], any[Format[CacheMap]]))
            .thenReturn(Future.successful(None))

          when(mockSessionService.save(ArgumentMatchers.eq(sessionCacheMap))(any[HeaderCarrier]))
            .thenReturn(Future.successful(sessionCacheMap))

          val res = await(service.initialiseJourney(testIntId, testRegId))

          res mustBe sessionCacheMap
        }
      }
    }
  }

}
