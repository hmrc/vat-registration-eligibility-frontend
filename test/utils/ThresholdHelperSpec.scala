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

package utils

import base.SpecBase
import identifiers.{ThresholdInTwelveMonthsId, ThresholdNextThirtyDaysId, ThresholdPreviousThirtyDaysId}
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap

class ThresholdHelperSpec extends SpecBase {

  "q1DefinedAndTrue" should {
    "return true if q1 is defined and true" in {
      val cacheMap = CacheMap("1",Map(ThresholdInTwelveMonthsId.toString -> Json.obj("value" -> true)))
      val userAnswers = new UserAnswers(cacheMap)
      ThresholdHelper.q1DefinedAndTrue(userAnswers) mustBe true
    }
    "return false if q1 is defined and false" in {
      val cacheMap = CacheMap("1",Map(ThresholdInTwelveMonthsId.toString -> Json.obj("value" -> false)))
      val userAnswers = new UserAnswers(cacheMap)
      ThresholdHelper.q1DefinedAndTrue(userAnswers) mustBe false
    }
    "return false if q1 is not defined" in {
      val cacheMap = CacheMap("1",Map())
      val userAnswers = new UserAnswers(cacheMap)
      ThresholdHelper.q1DefinedAndTrue(userAnswers) mustBe false
    }
  }

  "taxableTurnoverCheck" should {
    "return true if Q2 is defined and true, Q3 is defined and false" in {
      val cacheMap = CacheMap("1",Map(
        ThresholdNextThirtyDaysId.toString -> Json.obj("value" -> true),
        ThresholdPreviousThirtyDaysId.toString -> Json.obj("value" -> false)))
      val userAnswers = new UserAnswers(cacheMap)
      ThresholdHelper.taxableTurnoverCheck(userAnswers) mustBe true
    }
    "return true if Q2 is defined and false, Q3 is defined and true" in {
      val cacheMap = CacheMap("1",Map(
        ThresholdNextThirtyDaysId.toString -> Json.obj("value" -> false),
        ThresholdPreviousThirtyDaysId.toString ->  Json.obj("value" -> true)))
      val userAnswers = new UserAnswers(cacheMap)
      ThresholdHelper.taxableTurnoverCheck(userAnswers) mustBe true
    }
    "return false if Q2 and Q3 are not defined" in {
      val cacheMap = CacheMap("1",Map())
      val userAnswers = new UserAnswers(cacheMap)
      ThresholdHelper.taxableTurnoverCheck(userAnswers) mustBe false
    }
  }
}
