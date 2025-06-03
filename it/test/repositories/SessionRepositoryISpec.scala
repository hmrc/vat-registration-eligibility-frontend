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
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap

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

}
