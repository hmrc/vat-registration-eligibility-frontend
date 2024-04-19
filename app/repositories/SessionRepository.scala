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

package repositories

import com.mongodb.client.model.Indexes.ascending
import play.api.libs.json.OFormat
import org.mongodb.scala.model
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates.unset
import org.mongodb.scala.model._
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.play.http.logging.Mdc

import java.time.temporal.ChronoUnit
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class DatedCacheMap(id: String,
                         data: Map[String, JsValue],
                         lastUpdated: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS)) {
  def as[T](implicit f: DatedCacheMap => T): T = f(this)
}

object DatedCacheMap {
  implicit val formats: OFormat[DatedCacheMap] = Json.format[DatedCacheMap]

  def apply(cacheMap: CacheMap): DatedCacheMap = DatedCacheMap(cacheMap.id, cacheMap.data)

  implicit def asCacheMap: DatedCacheMap => CacheMap = (datedCacheMap: DatedCacheMap) => {
    CacheMap(datedCacheMap.id, datedCacheMap.data)
  }
}

@Singleton
class SessionRepository @Inject()(config: Configuration,
                                  mongo: MongoComponent)
                                 (implicit ec: ExecutionContext)
  extends PlayMongoRepository[DatedCacheMap](
    mongo,
    config.get[String]("appName"),
    DatedCacheMap.formats,
    Seq(
      model.IndexModel(
        ascending("lastUpdated"),
        IndexOptions()
          .name("userAnswersExpiry")
          .expireAfter(config.get[Int]("mongodb.timeToLiveInSeconds"), TimeUnit.SECONDS)
      )
    )
  ) {

  def get(id: String): Future[Option[CacheMap]] = Mdc.preservingMdc {
    collection.find(equal("id", id)).map(_.as[CacheMap]).headOption()
  }

  def upsert(cm: CacheMap): Future[Boolean] = Mdc.preservingMdc {
    collection.replaceOne(
      equal("id", cm.id),
      DatedCacheMap(cm),
      ReplaceOptions().upsert(true)
    ).map(_.wasAcknowledged()).head()
  }

  def removeEntry(id: String, key: String): Future[CacheMap] = Mdc.preservingMdc {
    collection.findOneAndUpdate(
      equal("id", id),
      unset(s"data.$key"),
      FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER).upsert(false)
    ).headOption().map(_.getOrElse(
      throw new Exception(s"[removeEntry] Attempted to remove $key but document did not exist")
    )).map(_.as[CacheMap])
  }

  def delete(id: String): Future[Boolean] = Mdc.preservingMdc {
    collection.deleteOne(equal("id", id)).map(_.wasAcknowledged()).head()
  }
}