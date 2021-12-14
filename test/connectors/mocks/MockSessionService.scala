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

package connectors.mocks

import connectors.SessionService
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Suite
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Format
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

trait MockSessionService extends MockitoSugar {
  self: Suite =>

  val sessionServiceMock = mock[SessionService]

  // Curried methods to allow for tidy setting of defaults for the different parameter lists
  def mockSessionCacheSave[A](cacheId: String, key: String): A => Future[CacheMap] => OngoingStubbing[Future[CacheMap]] =
    (value: A) => (response: Future[CacheMap]) => when(sessionServiceMock.save(
      ArgumentMatchers.eq(cacheId),
      ArgumentMatchers.eq(key),
      ArgumentMatchers.any[A]
    )(ArgumentMatchers.any[Format[A]])) thenReturn response

  def mockSessionCacheSave(cacheMap: CacheMap): OngoingStubbing[Future[CacheMap]] =
    when(sessionServiceMock.save(
      ArgumentMatchers.eq(cacheMap)
    )) thenReturn(Future.successful(cacheMap))

  def mockSessionFetch(cacheId: String): Future[Option[CacheMap]] => OngoingStubbing[Future[Option[CacheMap]]] =
    (response: Future[Option[CacheMap]]) => when(sessionServiceMock.fetch(
      ArgumentMatchers.eq(cacheId)
    )) thenReturn response

  def mockSessionGetEntry[A](cacheId: String, key: String): Future[Option[A]] => OngoingStubbing[Future[Option[A]]] =
    (response: Future[Option[A]]) => when(sessionServiceMock.getEntry(
      ArgumentMatchers.eq(cacheId),
      ArgumentMatchers.eq(key)
    )(ArgumentMatchers.any[Format[A]])) thenReturn response

  def mockClearSession(cacheId: String)(response: Future[Boolean]): OngoingStubbing[Future[Boolean]] =
    when(sessionServiceMock.delete(
      ArgumentMatchers.eq(cacheId)
    )) thenReturn response

}
