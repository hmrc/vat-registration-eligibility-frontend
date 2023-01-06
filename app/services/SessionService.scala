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

import com.google.inject.{ImplementedBy, Inject}
import play.api.libs.json.{Format, Json}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.CascadeUpsert

import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionServiceImpl @Inject()(val sessionRepository: SessionRepository,
                                   val cascadeUpsert: CascadeUpsert)
                                  (implicit ec: ExecutionContext) extends SessionService {

  def sessionId(implicit hc: HeaderCarrier): String =
    hc.sessionId.getOrElse(throw new RuntimeException("No session ID for current user")).value

  def save[A](key: String, value: A)(implicit fmt: Format[A], hc: HeaderCarrier): Future[CacheMap] = {
    sessionRepository.get(sessionId).flatMap { optionalCacheMap =>
      val updatedCacheMap = cascadeUpsert(key, value, optionalCacheMap.getOrElse(new CacheMap(sessionId, Map())))
      sessionRepository.upsert(updatedCacheMap).map { _ => updatedCacheMap }
    }
  }

  def save(cacheMap: CacheMap)(implicit hc: HeaderCarrier): Future[CacheMap] =
    sessionRepository.upsert(cacheMap).map (_ => cacheMap)

  def removeEntry(key: String)(implicit hc: HeaderCarrier): Future[CacheMap] = {
    sessionRepository.removeEntry(sessionId, key)
  }

  def delete(implicit hc: HeaderCarrier): Future[Boolean] = {
    sessionRepository.delete(sessionId)
  }

  def fetch(implicit hc: HeaderCarrier): Future[Option[CacheMap]] =
    sessionRepository.get(sessionId)

  def fetch(cacheId: String)(implicit hc: HeaderCarrier): Future[Option[CacheMap]] =
    sessionRepository.get(cacheId)

  def getEntry[A](key: String)(implicit fmt: Format[A], hc: HeaderCarrier): Future[Option[A]] = {
    fetch(sessionId).map { optionalCacheMap =>
      optionalCacheMap.flatMap { cacheMap => cacheMap.getEntry(key) }
    }
  }

  def addToCollection[A](collectionKey: String, value: A)(implicit fmt: Format[A], hc: HeaderCarrier): Future[CacheMap] = {
    sessionRepository.get(sessionId).flatMap { optionalCacheMap =>
      val updatedCacheMap = cascadeUpsert.addRepeatedValue(collectionKey, value, optionalCacheMap.getOrElse(new CacheMap(sessionId, Map())))
      sessionRepository.upsert(updatedCacheMap).map { _ => updatedCacheMap }
    }
  }

  def removeFromCollection[A](collectionKey: String, item: A)(implicit fmt: Format[A], hc: HeaderCarrier): Future[CacheMap] = {
    sessionRepository.get(sessionId).flatMap { optionalCacheMap =>
      optionalCacheMap.fold(throw new Exception(s"Couldn't find document in session $sessionId")) { cacheMap =>
        val newSeq = cacheMap.data(collectionKey).as[Seq[A]].filterNot(x => x == item)
        val newCacheMap = if (newSeq.isEmpty) {
          cacheMap copy (data = cacheMap.data - collectionKey)
        } else {
          cacheMap copy (data = cacheMap.data + (collectionKey -> Json.toJson(newSeq)))
        }

        sessionRepository.upsert(newCacheMap).map { _ => newCacheMap }
      }
    }
  }

  def replaceInCollection[A](collectionKey: String, index: Int, item: A)(implicit fmt: Format[A], hc: HeaderCarrier): Future[CacheMap] = {
    sessionRepository.get(sessionId).flatMap { optionalCacheMap =>
      optionalCacheMap.fold(throw new Exception(s"Couldn't find document in session $sessionId")) { cacheMap =>
        val newSeq = cacheMap.data(collectionKey).as[Seq[A]].updated(index, item)
        val updatedCacheMap = cacheMap copy (data = cacheMap.data + (collectionKey -> Json.toJson(newSeq)))
        sessionRepository.upsert(updatedCacheMap).map { _ => updatedCacheMap }
      }
    }
  }
}

@ImplementedBy(classOf[SessionServiceImpl])
trait SessionService {
  def sessionId(implicit hc: HeaderCarrier): String

  def save[A](key: String, value: A)(implicit fmt: Format[A], hc: HeaderCarrier): Future[CacheMap]

  def save(cacheMap: CacheMap)(implicit hc: HeaderCarrier): Future[CacheMap]

  def removeEntry(key: String)(implicit hc: HeaderCarrier): Future[CacheMap]

  def delete(implicit hc: HeaderCarrier): Future[Boolean]

  def fetch(implicit hc: HeaderCarrier): Future[Option[CacheMap]]

  def fetch(id: String)(implicit hc: HeaderCarrier): Future[Option[CacheMap]]

  def getEntry[A](key: String)(implicit fmt: Format[A], hc: HeaderCarrier): Future[Option[A]]

  def addToCollection[A](collectionKey: String, value: A)(implicit fmt: Format[A], hc: HeaderCarrier): Future[CacheMap]

  def removeFromCollection[A](collectionKey: String, item: A)(implicit fmt: Format[A], hc: HeaderCarrier): Future[CacheMap]

  def replaceInCollection[A](collectionKey: String, index: Int, item: A)(implicit fmt: Format[A], hc: HeaderCarrier): Future[CacheMap]
}