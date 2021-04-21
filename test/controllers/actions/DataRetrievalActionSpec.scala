/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.actions

import base.SpecBase
import connectors.DataCacheConnector
import connectors.mocks.MockS4lConnector
import models.CurrentProfile
import models.requests.{CacheIdentifierRequest, OptionalDataRequest}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class DataRetrievalActionSpec extends SpecBase with MockitoSugar with ScalaFutures with MockS4lConnector {

  class Harness(dataCacheConnector: DataCacheConnector) extends DataRetrievalActionImpl(dataCacheConnector, mockS4LConnector) {
    def callTransform[A](request: CacheIdentifierRequest[A]): Future[OptionalDataRequest[A]] = transform(request)
  }

  val testProfile = CurrentProfile("regId")
  val testCacheMap = CacheMap("id", Map("some" -> Json.obj("existing" -> "value")))

  "Data Retrieval Action" when {
    "there is no data in the cache" when {
      "when there is data in S4L" must {
        "set userAnswers to 'None' in the request" in {
          val dataCacheConnector = mock[DataCacheConnector]
          when(dataCacheConnector.fetch("id")) thenReturn Future(None)
          mockS4LFetchAll(Some(testCacheMap))
          when(dataCacheConnector.save(testCacheMap)) thenReturn Future(testCacheMap)

          val action = new Harness(dataCacheConnector)

          val futureResult = action.callTransform(new CacheIdentifierRequest(fakeRequest, "id", testProfile))

          whenReady(futureResult) { result =>
            result.userAnswers.isEmpty mustBe false
          }
        }
      }
      "when there is no data in S4L" must {
        "set userAnswers to 'None' in the request" in {
          val dataCacheConnector = mock[DataCacheConnector]
          when(dataCacheConnector.fetch("id")) thenReturn Future(None)
          mockS4LFetchAll(None)

          val action = new Harness(dataCacheConnector)

          val futureResult = action.callTransform(new CacheIdentifierRequest(fakeRequest, "id", testProfile))

          whenReady(futureResult) { result =>
            result.userAnswers.isEmpty mustBe true
          }
        }
      }
    }

    "there is data in the cache" must {
      "build a userAnswers object and add it to the request" in {
        val dataCacheConnector = mock[DataCacheConnector]
        when(dataCacheConnector.fetch("id")) thenReturn Future(Some(new CacheMap("id", Map())))
        val action = new Harness(dataCacheConnector)

        val futureResult = action.callTransform(new CacheIdentifierRequest(fakeRequest, "id", testProfile))

        whenReady(futureResult) { result =>
          result.userAnswers.isDefined mustBe true
        }
      }
    }
  }
}
