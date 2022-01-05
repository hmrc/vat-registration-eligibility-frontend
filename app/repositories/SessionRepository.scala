/*
 * Copyright 2022 HM Revenue & Customs
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

import org.joda.time.{DateTime, DateTimeZone}
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONInteger, BSONObjectID}
import reactivemongo.play.json.ImplicitBSONHandlers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.play.http.logging.Mdc

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class DatedCacheMap(id: String,
                         data: Map[String, JsValue],
                         lastUpdated: DateTime = DateTime.now(DateTimeZone.UTC))

object DatedCacheMap {
  implicit val dateFormat = ReactiveMongoFormats.dateTimeFormats
  implicit val formats = Json.format[DatedCacheMap]

  def apply(cacheMap: CacheMap): DatedCacheMap = DatedCacheMap(cacheMap.id, cacheMap.data)
}

@Singleton
class SessionRepository @Inject()(config: Configuration,
                                  mongo: ReactiveMongoComponent)
                                 (implicit ec: ExecutionContext)
  extends ReactiveRepository[DatedCacheMap, BSONObjectID](
    config.get[String]("appName"),
    mongo.mongoConnector.db,
    DatedCacheMap.formats) {

  val fieldName = "lastUpdated"
  val createdIndexName = "userAnswersExpiry"
  val expireAfterSeconds = "expireAfterSeconds"
  val timeToLiveInSeconds: Int = config.get[Int]("mongodb.timeToLiveInSeconds")

  val lastModifiedIndex = Index(
    name = Some(createdIndexName),
    key = Seq(fieldName -> IndexType.Ascending),
    options = BSONDocument(expireAfterSeconds -> BSONInteger(timeToLiveInSeconds))
  )

  override def indexes: Seq[Index] = Seq(lastModifiedIndex)

  override def ensureIndexes(implicit ec: ExecutionContext): Future[Seq[Boolean]] = {
    def deleteLastUpdatedIndex(indexes: List[Index])(implicit ec: ExecutionContext): Future[Int] = {
      indexes.find(index => index.eventualName == "lastModified") match {
        case Some(index) => collection.indexesManager.drop(index.eventualName)
        case None => Future.successful(0)
      }
    }

    for {
      currentIndexes <- collection.indexesManager.list()
      _<- deleteLastUpdatedIndex(currentIndexes)
      indexes = currentIndexes :+ lastModifiedIndex
      updated <- Future.sequence(indexes.map(collection.indexesManager.ensure))
    } yield updated
  }

  def get(id: String): Future[Option[CacheMap]] = Mdc.preservingMdc {
    collection.find(Json.obj("id" -> id)).one[CacheMap]
  }

  def upsert(cm: CacheMap): Future[Boolean] = Mdc.preservingMdc {
    val selector = BSONDocument("id" -> cm.id)
    val cmDocument = Json.toJson(DatedCacheMap(cm))
    val modifier = BSONDocument("$set" -> cmDocument)

    collection.update(selector, modifier, upsert = true).map { lastError =>
      lastError.ok
    }
  }

  def removeEntry(id: String, key: String): Future[CacheMap] = Mdc.preservingMdc {
    val selector = BSONDocument("id" -> id)
    val update = BSONDocument("$unset" -> BSONDocument(s"data.$key" -> 1))

    collection.findAndModify(selector, collection.updateModifier(update, true, false)).map {
      res => res.value.map(_.as[CacheMap]).getOrElse(throw new Exception(s"[removeEntry] Attempted to remove $key but document did not exist"))
    }
  }

  def delete(id: String): Future[Boolean] = Mdc.preservingMdc {
    val selector = BSONDocument("id" -> id)

    collection.delete.one(selector).map(_.ok)
  }

}

