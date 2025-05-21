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

import helpers.IntegrationSpecBase
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.bson.{BsonBoolean, BsonDateTime, BsonDocument, BsonString}
import org.mongodb.scala.{MongoCollection, result}
import play.api.libs.json.{JsBoolean, Json}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.mongo.MongoComponent

import java.time.Instant
import java.time.temporal.ChronoUnit

class SessionRepositoryISpec extends IntegrationSpecBase {

  class Setup {
    val newMongoInstance = app.injector.instanceOf[SessionRepository]

    val record1         = CacheMap("1", Map("foo" -> Json.obj("" -> "")))
    val record2         = CacheMap("2", Map("foo" -> Json.obj("" -> "")))
    val record3         = CacheMap("3", Map("foo" -> Json.obj("wuzz" -> "buzz"), "bar" -> Json.obj("fudge" -> "wizz")))
    val record3_updated = CacheMap("3", Map("bar" -> Json.obj("fudge" -> "wizz")))
    val record4         = CacheMap("4", Map("foo" -> Json.obj("wuzz" -> "buzz"), "bar" -> Json.obj("fudge" -> "wizz")))
  }

  "removeEntry" must {
    "throw an exception if the document does not exist in the repository" in new Setup {
      await(newMongoInstance.collection.drop().head())
      intercept[Exception](await(newMongoInstance.removeEntry("int1", "bar")))
    }
    "update the document to unset the appropriate key with other records in the db return document" in new Setup {
      await(newMongoInstance.collection.drop().head())
      await(newMongoInstance.upsert(record4))
      await(newMongoInstance.upsert(record3))
      await(newMongoInstance.collection.countDocuments().head()) mustBe 2
      await(newMongoInstance.removeEntry("3", "foo")) mustBe record3_updated
      await(newMongoInstance.collection.countDocuments().head()) mustBe 2
      await(newMongoInstance.get("4")) mustBe Some(record4)

    }
    "update the document to unset a key that does not exist in mongo. return document" in new Setup {
      await(newMongoInstance.collection.drop().head())
      await(newMongoInstance.upsert(record1))
      await(newMongoInstance.upsert(record3))
      await(newMongoInstance.collection.countDocuments().head()) mustBe 2
      await(newMongoInstance.removeEntry("3", "doesNotExist")) mustBe record3
      await(newMongoInstance.collection.countDocuments().head()) mustBe 2
    }
  }

  trait DeleteDataSetup {
    val testSessionRepository: SessionRepository        = app.injector.instanceOf[SessionRepository]
    val mongoCollection: MongoCollection[DatedCacheMap] = testSessionRepository.collection

    val mongoComponent: MongoComponent                = app.injector.instanceOf[MongoComponent]
    val rawMongoCollection: MongoCollection[Document] = mongoComponent.database.getCollection[Document]("vat-registration-eligibility-frontend")

    def fillDatabaseWithData(rawDataToAdd: Seq[Document]): result.InsertManyResult = {
      await(rawMongoCollection.drop().head())
      await(rawMongoCollection.insertMany(rawDataToAdd).toFuture())
    }
  }

  private val validData1 = DatedCacheMap(
    id = "valid-id-1",
    data = Map("voluntaryRegistration" -> JsBoolean(true)),
    lastUpdated = Instant.now()
  )
  private val validData2 = DatedCacheMap(
    id = "valid-id-2",
    data = Map("voluntaryRegistration" -> JsBoolean(false)),
    lastUpdated = Instant.now().minusSeconds(3600)
  )
  private val invalidData1 = Document(
    "id"          -> BsonString("invalid-1"),
    "data"        -> BsonDocument("voluntaryRegistration" -> BsonBoolean(true)),
    "lastUpdated" -> BsonString("2020-01-01T00:00:00Z"), // This is the invalid data type, is String not BsonDateTime
    "expiry"      -> BsonDateTime(Instant.now.plus(3600, ChronoUnit.SECONDS).toEpochMilli)
  )
  private val validDataSetDocs: Seq[Document] = convertDatedCacheMapsToDocuments(Seq(validData1, validData2))

  private def convertDatedCacheMapsToDocuments(datedCacheMaps: Seq[DatedCacheMap]): Seq[Document] =
    datedCacheMaps.map(dcm =>
      Document(
        "id" -> BsonString(dcm.id),
        "data" -> BsonDocument(dcm.data.map { case (key, jsValue: JsBoolean) =>
          key -> BsonBoolean(jsValue.value)
        }),
        "lastUpdated" -> BsonDateTime(dcm.lastUpdated.toEpochMilli), // This is the valid data type
        "expiry"      -> BsonDateTime(Instant.now.plus(3600, ChronoUnit.SECONDS).toEpochMilli)
      ))

  "deleteDataWithLastUpdatedStringType" must {
    "delete any data with a 'lastUpdated' index of type String" when {
      "database is empty" in new DeleteDataSetup {
        await(mongoCollection.drop().head())
        await(mongoCollection.countDocuments().head()) mustBe 0

        await(testSessionRepository.deleteDataWithLastUpdatedStringType()).getDeletedCount mustBe 0
        await(testSessionRepository.collection.countDocuments().head()) mustBe 0
      }

      "database has only valid data" in new DeleteDataSetup {
        fillDatabaseWithData(validDataSetDocs)
        await(mongoCollection.countDocuments().head()) mustBe 2

        await(testSessionRepository.deleteDataWithLastUpdatedStringType()).getDeletedCount mustBe 0
        await(testSessionRepository.collection.countDocuments().head()) mustBe 2
      }

      "database has only invalid data" in new DeleteDataSetup {
        fillDatabaseWithData(Seq(invalidData1))
        await(mongoCollection.countDocuments().head()) mustBe 1

        await(testSessionRepository.deleteDataWithLastUpdatedStringType()).getDeletedCount mustBe 1
        await(testSessionRepository.collection.countDocuments().head()) mustBe 0
      }

      "database has a mix of valid and invalid data" in new DeleteDataSetup {
        fillDatabaseWithData(Seq(invalidData1) ++ validDataSetDocs)
        await(mongoCollection.countDocuments().head()) mustBe 3

        await(testSessionRepository.deleteDataWithLastUpdatedStringType()).getDeletedCount mustBe 1
        await(testSessionRepository.collection.countDocuments().head()) mustBe 2
      }
    }
  }
}
