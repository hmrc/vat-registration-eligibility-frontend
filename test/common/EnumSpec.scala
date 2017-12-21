/*
 * Copyright 2017 HM Revenue & Customs
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

package common.enums

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsString, JsSuccess, Json}

class EnumSpec extends PlaySpec {

  "private Version1EligibilityResult" should {
    "Version1EligibilityResultValueToString return a valid String" in {
      Version1EligibilityResult.Version1EligibilityResultValueToString(Version1EligibilityResult.noNino) mustBe "noNino"
    }
    "format to and from json correctly" in {
      val json = JsString("noNino")

      Json.toJson(Version1EligibilityResult.format.writes(Version1EligibilityResult.noNino)) mustBe json
      Version1EligibilityResult.format.reads(Json.toJson("noNino")) mustBe JsSuccess(Version1EligibilityResult.noNino)
    }
  }
  "EligibilityResult questions" should {
    "return the correct private Enum based on version number" in {
      EligibilityResult(1).questions mustBe Version1EligibilityResult
    }
  }

  "VatRegStatus" should {
    "format to and json correctly" in {
      Json.toJson(VatRegStatus.format.writes(VatRegStatus.draft)) mustBe JsString("draft")
      VatRegStatus.format.reads(Json.toJson("draft")) mustBe JsSuccess(VatRegStatus.draft)
    }
  }

  "CacheKeys" should {
    "cacheKeysValueToString return a valid string" in {
      CacheKeys.cacheKeysValueToString(CacheKeys.CurrentProfile) mustBe "CurrentProfile"
    }
  }

}