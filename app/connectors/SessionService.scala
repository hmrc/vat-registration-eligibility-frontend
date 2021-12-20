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

package connectors

import com.google.inject.{ImplementedBy, Inject}
import play.api.libs.json.{Format, Json}
import repositories.SessionRepository
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.CascadeUpsert

import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionServiceImpl @Inject()(val sessionRepository: SessionRepository,
                                   val cascadeUpsert: CascadeUpsert)
                                  (implicit ec: ExecutionContext) extends SessionService {

  def save[A](cacheId: String, key: String, value: A)(implicit fmt: Format[A]): Future[CacheMap] = {
    sessionRepository.get(cacheId).flatMap { optionalCacheMap =>
      val updatedCacheMap = cascadeUpsert(key, value, optionalCacheMap.getOrElse(new CacheMap(cacheId, Map())))
      sessionRepository.upsert(updatedCacheMap).map { _ => updatedCacheMap }
    }
  }

  def save(cacheMap: CacheMap): Future[CacheMap] =
    sessionRepository.upsert(cacheMap) map (_ => cacheMap)

  def removeEntry(cacheId: String, key: String): Future[CacheMap] = {
    sessionRepository.removeEntry(cacheId, key)
  }

  def delete(cacheId: String): Future[Boolean] = {
    sessionRepository.delete(cacheId)
  }

  def fetch(cacheId: String): Future[Option[CacheMap]] =
    sessionRepository.get(cacheId)

  def getEntry[A](cacheId: String, key: String)(implicit fmt: Format[A]): Future[Option[A]] = {
    fetch(cacheId).map { optionalCacheMap =>
      optionalCacheMap.flatMap { cacheMap => cacheMap.getEntry(key) }
    }
  }

  def addToCollection[A](cacheId: String, collectionKey: String, value: A)(implicit fmt: Format[A]): Future[CacheMap] = {
    sessionRepository.get(cacheId).flatMap { optionalCacheMap =>
      val updatedCacheMap = cascadeUpsert.addRepeatedValue(collectionKey, value, optionalCacheMap.getOrElse(new CacheMap(cacheId, Map())))
      sessionRepository.upsert(updatedCacheMap).map { _ => updatedCacheMap }
    }
  }

  def removeFromCollection[A](cacheId: String, collectionKey: String, item: A)(implicit fmt: Format[A]): Future[CacheMap] = {
    sessionRepository.get(cacheId).flatMap { optionalCacheMap =>
      optionalCacheMap.fold(throw new Exception(s"Couldn't find document with key $cacheId")) { cacheMap =>
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

  def replaceInCollection[A](cacheId: String, collectionKey: String, index: Int, item: A)(implicit fmt: Format[A]): Future[CacheMap] = {
    sessionRepository.get(cacheId).flatMap { optionalCacheMap =>
      optionalCacheMap.fold(throw new Exception(s"Couldn't find document with key $cacheId")) { cacheMap =>
        val newSeq = cacheMap.data(collectionKey).as[Seq[A]].updated(index, item)
        val updatedCacheMap = cacheMap copy (data = cacheMap.data + (collectionKey -> Json.toJson(newSeq)))
        sessionRepository.upsert(updatedCacheMap).map { _ => updatedCacheMap }
      }
    }
  }
}

@ImplementedBy(classOf[SessionServiceImpl])
trait SessionService {
  def save[A](cacheId: String, key: String, value: A)(implicit fmt: Format[A]): Future[CacheMap]

  def save(cacheMap: CacheMap): Future[CacheMap]

  def removeEntry(cacheId: String, key: String): Future[CacheMap]

  def delete(cacheId: String): Future[Boolean]

  def fetch(cacheId: String): Future[Option[CacheMap]]

  def getEntry[A](cacheId: String, key: String)(implicit fmt: Format[A]): Future[Option[A]]

  def addToCollection[A](cacheId: String, collectionKey: String, value: A)(implicit fmt: Format[A]): Future[CacheMap]

  def removeFromCollection[A](cacheId: String, collectionKey: String, item: A)(implicit fmt: Format[A]): Future[CacheMap]

  def replaceInCollection[A](cacheId: String, collectionKey: String, index: Int, item: A)(implicit fmt: Format[A]): Future[CacheMap]
}