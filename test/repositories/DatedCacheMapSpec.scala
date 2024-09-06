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

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json

import java.time.{LocalDateTime, ZoneId}

class DatedCacheMapSpec extends PlaySpec {

  "DatedCacheMap" must {
    "serialize and deserialize lastUpdated" in {
      val localDate = LocalDateTime.parse("2024-04-19T09:45:59.365")
      val lastUpdated = localDate.atZone(ZoneId.systemDefault()).toInstant
      val datedCacheMap = DatedCacheMap("id", Map.empty, lastUpdated)
      Json.toJson(datedCacheMap).as[DatedCacheMap] mustBe datedCacheMap
      datedCacheMap.lastUpdated.toString mustBe "2024-04-19T09:45:59.365Z"
    }
  }
}