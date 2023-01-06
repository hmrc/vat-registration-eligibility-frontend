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

package connectors.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.Suite
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Format
import services.SessionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

trait MockSessionService extends MockitoSugar {
  self: Suite =>

  val sessionServiceMock = mock[SessionService]

  // Curried methods to allow for tidy setting of defaults for the different parameter lists
  def mockSessionCacheSave[A](key: String): A => Future[CacheMap] => OngoingStubbing[Future[CacheMap]] =
    (value: A) => (response: Future[CacheMap]) => when(sessionServiceMock.save(
      ArgumentMatchers.eq(key),
      ArgumentMatchers.any[A]
    )(ArgumentMatchers.any[Format[A]], ArgumentMatchers.any[HeaderCarrier])) thenReturn response

  def mockSessionCacheSave(cacheMap: CacheMap): OngoingStubbing[Future[CacheMap]] =
    when(sessionServiceMock.save(
      ArgumentMatchers.eq(cacheMap)
    )(ArgumentMatchers.any[HeaderCarrier])) thenReturn(Future.successful(cacheMap))

  def mockSessionFetch(): Future[Option[CacheMap]] => OngoingStubbing[Future[Option[CacheMap]]] =
    (response: Future[Option[CacheMap]]) => when(sessionServiceMock.fetch(ArgumentMatchers.any[HeaderCarrier])) thenReturn response

  def mockSessionFetch(cacheId: String): Future[Option[CacheMap]] => OngoingStubbing[Future[Option[CacheMap]]] =
    (response: Future[Option[CacheMap]]) => when(sessionServiceMock.fetch(
      ArgumentMatchers.eq(cacheId)
    )(ArgumentMatchers.any[HeaderCarrier])) thenReturn response

  def mockSessionGetEntry[A](key: String): Future[Option[A]] => OngoingStubbing[Future[Option[A]]] =
    (response: Future[Option[A]]) => when(sessionServiceMock.getEntry(
      ArgumentMatchers.eq(key)
    )(ArgumentMatchers.any[Format[A]], ArgumentMatchers.any[HeaderCarrier])) thenReturn response

  def mockSessionRemoveEntry[A](key: String): Future[CacheMap] => OngoingStubbing[Future[CacheMap]] =
    (response: Future[CacheMap]) => when(sessionServiceMock.removeEntry(
      ArgumentMatchers.eq(key)
    )(ArgumentMatchers.any[HeaderCarrier])) thenReturn response

  def mockClearSession(response: Future[Boolean]): OngoingStubbing[Future[Boolean]] =
    when(sessionServiceMock.delete(ArgumentMatchers.any[HeaderCarrier])) thenReturn response

}
