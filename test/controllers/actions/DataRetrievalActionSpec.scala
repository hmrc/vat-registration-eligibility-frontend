/*
 * Copyright 2024 HM Revenue & Customs
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
import connectors.mocks.MockSessionService
import models.CurrentProfile
import models.requests.{CacheIdentifierRequest, OptionalDataRequest}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import services.SessionService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

import scala.concurrent.Future

class DataRetrievalActionSpec extends SpecBase with MockitoSugar with ScalaFutures with MockSessionService {

  class Harness(sessionService: SessionService) extends DataRetrievalActionImpl(sessionService) {
    def callTransform[A](request: CacheIdentifierRequest[A]): Future[OptionalDataRequest[A]] = transform(request)
  }

  val testProfile = CurrentProfile("regId")
  val testCacheMap = CacheMap("sessionId", Map("some" -> Json.obj("existing" -> "value")))

  override implicit val hc = HeaderCarrier(sessionId = Some(SessionId("sessionId")))

  "Data Retrieval Action" when {
    "there is no data in the cache" when {
      "set userAnswers to 'None' in the request" in {
        mockSessionFetch()(Future.successful(None))

        val action = new Harness(sessionServiceMock)

        val futureResult = action.callTransform(new CacheIdentifierRequest(fakeRequest, "regId", "id"))

        whenReady(futureResult) { result =>
          result.userAnswers.isEmpty mustBe true
        }
      }
    }

    "there is data in the cache" must {
      "build a userAnswers object and add it to the request" in {
        mockSessionFetch()(Future.successful(Some(new CacheMap("regId", Map()))))
        val action = new Harness(sessionServiceMock)

        val futureResult = action.callTransform(new CacheIdentifierRequest(fakeRequest, "regId", "id"))

        whenReady(futureResult) { result =>
          result.userAnswers.isDefined mustBe true
        }
      }
    }
  }
}
