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

import play.api.libs.json.Format
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object FakeSessionService extends SessionService {

  def sessionId(implicit hc: HeaderCarrier): String = "sessionId"

  def save(cacheMap: CacheMap)(implicit hc: HeaderCarrier): Future[CacheMap] = Future.successful(cacheMap)

  def save[A](key: String, value: A)(implicit fmt: Format[A], hc: HeaderCarrier): Future[CacheMap] = Future(CacheMap(sessionId, Map()))

  def fetch(cacheId: String)(implicit hc: HeaderCarrier): Future[Option[CacheMap]] = Future(Some(CacheMap(sessionId, Map())))

  def fetch(implicit hc: HeaderCarrier): Future[Option[CacheMap]] = Future(Some(CacheMap(sessionId, Map())))

  def delete(implicit hc: HeaderCarrier): Future[Boolean] = Future.successful(true)

  def getEntry[A](key: String)(implicit fmt: Format[A], hc: HeaderCarrier): Future[Option[A]] = ???

  def addToCollection[A](collectionKey: String, value: A)(implicit fmt: Format[A], hc: HeaderCarrier): Future[CacheMap] = Future(CacheMap(sessionId, Map()))

  def removeFromCollection[A](collectionKey: String, item: A)(implicit fmt: Format[A], hc: HeaderCarrier): Future[CacheMap] = Future(CacheMap(sessionId, Map()))

  def replaceInCollection[A](collectionKey: String, index: Int, item: A)(implicit fmt: Format[A], hc: HeaderCarrier): Future[CacheMap] = Future(CacheMap(sessionId, Map()))

  def removeEntry(key: String)(implicit hc: HeaderCarrier): Future[CacheMap] = Future(CacheMap(sessionId, Map()))

}
