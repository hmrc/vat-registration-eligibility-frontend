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

import config.FrontendAppConfig
import helpers.IntegrationSpecBase
import org.mockito.Mockito.when
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.bson.{BsonBoolean, BsonDateTime, BsonDocument, BsonString}
import org.mongodb.scala.{MongoCollection, result}
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.{JsBoolean, Json}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.Instant
import java.time.temporal.ChronoUnit

class SessionRepositoryISpec extends IntegrationSpecBase with TableDrivenPropertyChecks {

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

  private val mockAppConfig: FrontendAppConfig   = mock[FrontendAppConfig]
  private val mockServicesConfig: ServicesConfig = mock[ServicesConfig]
  when(mockAppConfig.servicesConfig).thenReturn(mockServicesConfig)

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
  private val invalidData = Document(
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

  "deleteAllDataWithLastUpdatedStringType" must {
    "delete any data with a 'lastUpdated' index of type String" when {
      "database is empty" in new DeleteDataSetup {
        await(mongoCollection.drop().head())
        await(mongoCollection.countDocuments().head()) mustBe 0

        await(testSessionRepository.deleteAllDataWithLastUpdatedStringType()).getDeletedCount mustBe 0
        await(testSessionRepository.collection.countDocuments().head()) mustBe 0
      }

      "database has only valid data" in new DeleteDataSetup {
        fillDatabaseWithData(validDataSetDocs)
        await(mongoCollection.countDocuments().head()) mustBe 2

        await(testSessionRepository.deleteAllDataWithLastUpdatedStringType()).getDeletedCount mustBe 0
        await(testSessionRepository.collection.countDocuments().head()) mustBe 2
      }

      "database has only invalid data" in new DeleteDataSetup {
        fillDatabaseWithData(Seq(invalidData))
        await(mongoCollection.countDocuments().head()) mustBe 1

        await(testSessionRepository.deleteAllDataWithLastUpdatedStringType()).getDeletedCount mustBe 1
        await(testSessionRepository.collection.countDocuments().head()) mustBe 0
      }

      "database has a mix of valid and invalid data" in new DeleteDataSetup {
        fillDatabaseWithData(Seq(invalidData) ++ validDataSetDocs)
        await(mongoCollection.countDocuments().head()) mustBe 3

        await(testSessionRepository.deleteAllDataWithLastUpdatedStringType()).getDeletedCount mustBe 1
        await(testSessionRepository.collection.countDocuments().head()) mustBe 2
      }
    }
  }

  "deleteNDataWithLastUpdatedStringType" must {
    "delete data - up to config limit - with a 'lastUpdated' index of type String" when {
      "database is empty" when {
        "delete limit is zero" in new DeleteDataSetup {
          val limit = 0
          await(mongoCollection.drop().head())
          await(mongoCollection.countDocuments().head()) mustBe 0

          await(testSessionRepository.deleteNDataWithLastUpdatedStringType(limit)).getDeletedCount mustBe 0
          await(testSessionRepository.collection.countDocuments().head()) mustBe 0
        }
        "delete limit exceeds document count" in new DeleteDataSetup {
          val limit = 2
          await(mongoCollection.drop().head())
          await(mongoCollection.countDocuments().head()) mustBe 0

          await(testSessionRepository.deleteNDataWithLastUpdatedStringType(limit)).getDeletedCount mustBe 0
          await(testSessionRepository.collection.countDocuments().head()) mustBe 0
        }
      }

      "database has only valid data, no data should be deleted" in new DeleteDataSetup {
        val limit = 2
        fillDatabaseWithData(validDataSetDocs)
        await(mongoCollection.countDocuments().head()) mustBe 2

        await(testSessionRepository.deleteNDataWithLastUpdatedStringType(limit)).getDeletedCount mustBe 0
        await(testSessionRepository.collection.countDocuments().head()) mustBe 2
      }

      "database has only invalid data" when {
        "limit is zero" in new DeleteDataSetup {
          val limit = 0
          fillDatabaseWithData(Seq(invalidData, invalidData))
          await(mongoCollection.countDocuments().head()) mustBe 2

          await(testSessionRepository.deleteNDataWithLastUpdatedStringType(limit)).getDeletedCount mustBe 0
          await(testSessionRepository.collection.countDocuments().head()) mustBe 2
        }
        "limit is greater than invalid doc count" in new DeleteDataSetup {
          val limit = 5
          fillDatabaseWithData(Seq(invalidData, invalidData))
          await(mongoCollection.countDocuments().head()) mustBe 2

          await(testSessionRepository.deleteNDataWithLastUpdatedStringType(limit)).getDeletedCount mustBe 2
          await(testSessionRepository.collection.countDocuments().head()) mustBe 0
        }
        "limit is less than invalid doc count" in new DeleteDataSetup {
          val limit = 1
          fillDatabaseWithData(Seq(invalidData, invalidData))
          await(mongoCollection.countDocuments().head()) mustBe 2

          await(testSessionRepository.deleteNDataWithLastUpdatedStringType(limit)).getDeletedCount mustBe 1
          await(testSessionRepository.collection.countDocuments().head()) mustBe 1
        }
      }

      "database has a mix of valid and invalid data" when {
        "limit is zero" in new DeleteDataSetup {
          val limit = 0
          fillDatabaseWithData(validDataSetDocs ++ Seq(invalidData, invalidData))
          await(mongoCollection.countDocuments().head()) mustBe 4

          await(testSessionRepository.deleteNDataWithLastUpdatedStringType(limit)).getDeletedCount mustBe 0
          await(testSessionRepository.collection.countDocuments().head()) mustBe 4
        }
        "limit is greater than invalid doc count" in new DeleteDataSetup {
          val limit = 5
          fillDatabaseWithData(validDataSetDocs ++ Seq(invalidData, invalidData))
          await(mongoCollection.countDocuments().head()) mustBe 4

          await(testSessionRepository.deleteNDataWithLastUpdatedStringType(limit)).getDeletedCount mustBe 2
          await(testSessionRepository.collection.countDocuments().head()) mustBe 2
        }
        "limit is less than invalid doc count" in new DeleteDataSetup {
          val limit = 1
          fillDatabaseWithData(validDataSetDocs ++ Seq(invalidData, invalidData))
          await(mongoCollection.countDocuments().head()) mustBe 4

          await(testSessionRepository.deleteNDataWithLastUpdatedStringType(limit)).getDeletedCount mustBe 1
          await(testSessionRepository.collection.countDocuments().head()) mustBe 3
        }
      }
    }
  }

}
