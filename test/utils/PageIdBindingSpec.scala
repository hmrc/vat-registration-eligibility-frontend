/*
 * Copyright 2020 HM Revenue & Customs
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

import identifiers.{ThresholdInTwelveMonthsId, _}
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsBoolean, JsString, JsValue, Json}
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.collection.immutable.ListMap

class PageIdBindingSpec extends PlaySpec {
  val fullListMapHappyPathTwelveMonthsFalse: ListMap[String, JsValue] = ListMap[String, JsValue](
    "" -> JsString(""),
    s"$ThresholdInTwelveMonthsId" -> Json.obj("value" -> JsBoolean(false)),
    s"$ThresholdNextThirtyDaysId" -> Json.obj("value" -> JsBoolean(false)),
    s"$ThresholdPreviousThirtyDaysId" -> Json.obj("value" -> JsBoolean(false)),
    s"$VoluntaryRegistrationId" -> JsBoolean(true),
    s"$TurnoverEstimateId" -> Json.obj("amount" -> JsString("50000")),
    s"$InternationalActivitiesId" -> JsBoolean(false),
    s"$InvolvedInOtherBusinessId" -> JsBoolean(false),
    s"$AnnualAccountingSchemeId" -> JsBoolean(false),
    s"$VoluntaryRegistrationId" -> JsBoolean(true),
    s"$VATExemptionId" -> JsBoolean(false),
    s"$VATRegistrationExceptionId" -> JsBoolean(false),
    s"$AgriculturalFlatRateSchemeId" -> JsBoolean(false),
    s"$RacehorsesId" -> JsBoolean(false)
  )
  fullListMapHappyPathTwelveMonthsFalse.foldLeft(Map[String, JsValue]()) {
    case (mockedReturn, currentItem) =>
      s"an exception should be experienced when only pages before ${currentItem._1} have been filled" in {
        intercept[Exception](PageIdBinding.sectionBindings(new CacheMap("testId", mockedReturn)))
      }
      mockedReturn + currentItem
  }
  val listMapWithoutFieldsToBeTested = fullListMapHappyPathTwelveMonthsFalse.filterNot { s =>
    s._1 match {
      case x if x == ThresholdInTwelveMonthsId.toString || x == ThresholdNextThirtyDaysId.toString ||
        x == ThresholdPreviousThirtyDaysId.toString || x == VoluntaryRegistrationId.toString ||
        x == VATExemptionId.toString || x == ZeroRatedSalesId.toString || x == VATRegistrationExceptionId.toString => true
      case _ => false
    }
  }

  "no exception should be thrown when a cacheMap containing ThresholdTwelveMonths == true, ThresholdNextThirty doesn't exist" in {
    val mapOfValuesToBeTested = List(
      s"$ThresholdInTwelveMonthsId" -> Json.obj("value" -> JsBoolean(true)),
      s"$ThresholdPreviousThirtyDaysId" -> Json.obj("value" -> JsBoolean(false)),
      s"$VATExemptionId" -> JsBoolean(false),
      s"$ZeroRatedSalesId" -> JsBoolean(true),
      s"$RegisteringBusinessId" -> JsBoolean(true),
      s"$NinoId" -> JsBoolean(true),
      s"$VATRegistrationExceptionId" -> JsBoolean(false)
    )
    PageIdBinding.sectionBindings(new CacheMap("test", listMapWithoutFieldsToBeTested.++:(mapOfValuesToBeTested)))
  }
  "exception should be thrown when a cacheMap containing ThresholdTwelveMonths == true, ThresholdNextThirty does exist" in {
    val mapOfValuesToBeTested = List(
      s"$ThresholdInTwelveMonthsId" -> Json.obj("value" -> JsBoolean(true)),
      s"$ThresholdNextThirtyDaysId" -> JsBoolean(true),
      s"$ThresholdPreviousThirtyDaysId" -> Json.obj("value" -> JsBoolean(false)),
      s"$VATExemptionId" -> JsBoolean(false),
      s"$ZeroRatedSalesId" -> JsBoolean(true),
      s"$VATRegistrationExceptionId" -> JsBoolean(false)
    )
    intercept[Exception](PageIdBinding.sectionBindings(new CacheMap("test", listMapWithoutFieldsToBeTested.++:(mapOfValuesToBeTested))))
  }
  "exception should be thrown when a cacheMap containing zero rated sales == false, Vat exemption exists" in {
    val mapOfValuesToBeTested = List(
      s"$ThresholdInTwelveMonthsId" -> Json.obj("value" -> JsBoolean(true)),
      s"$ThresholdPreviousThirtyDaysId" -> Json.obj("value" -> JsBoolean(false)),
      s"$VATExemptionId" -> JsBoolean(false),
      s"$ZeroRatedSalesId" -> JsBoolean(false),
      s"$VATRegistrationExceptionId" -> JsBoolean(false)
    )
    intercept[Exception](PageIdBinding.sectionBindings(new CacheMap("test", listMapWithoutFieldsToBeTested.++:(mapOfValuesToBeTested))))
  }
  "no exception should be thrown when a cacheMap containing zero rated sales == false, Vat exemption does not exist" in {
    val mapOfValuesToBeTested = List(
      s"$ThresholdInTwelveMonthsId" -> Json.obj("value" -> JsBoolean(true)),
      s"$ThresholdPreviousThirtyDaysId" -> Json.obj("value" -> JsBoolean(false)),
      s"$ZeroRatedSalesId" -> JsBoolean(false),
      s"$RegisteringBusinessId" -> JsBoolean(true),
      s"$NinoId" -> JsBoolean(true),
      s"$VATRegistrationExceptionId" -> JsBoolean(false)
    )
    PageIdBinding.sectionBindings(new CacheMap("test", listMapWithoutFieldsToBeTested.++:(mapOfValuesToBeTested)))
  }
  "throw new exception if voluntary flag does not exist when all 3 threshold q's are no" in {
    val mapOfValuesToBeTested = List(
      s"$ThresholdInTwelveMonthsId" -> Json.obj("value" -> JsBoolean(false)),
      s"$ThresholdNextThirtyDaysId" -> JsBoolean(false),
      s"$ThresholdPreviousThirtyDaysId" -> Json.obj("value" -> JsBoolean(false)),
      s"$VATExemptionId" -> JsBoolean(false),
      s"$ZeroRatedSalesId" -> JsBoolean(true)
    )
    intercept[Exception](PageIdBinding.sectionBindings(new CacheMap("test", listMapWithoutFieldsToBeTested.++:(mapOfValuesToBeTested))))
  }
  "throw exception if all 3 threshold q's exist, one answer == true, voluntary flag exists" in {
    val mapOfValuesToBeTested = List(
      s"$ThresholdInTwelveMonthsId" -> Json.obj("value" -> JsBoolean(true)),
      s"$ThresholdNextThirtyDaysId" -> JsBoolean(false),
      s"$ThresholdPreviousThirtyDaysId" -> Json.obj("value" -> JsBoolean(false)),
      s"$VoluntaryRegistrationId" -> JsBoolean(true),
      s"$VATExemptionId" -> JsBoolean(false),
      s"$ZeroRatedSalesId" -> JsBoolean(true),
      s"$VATRegistrationExceptionId" -> JsBoolean(false)
    )
    intercept[Exception](PageIdBinding.sectionBindings(new CacheMap("test", listMapWithoutFieldsToBeTested.++:(mapOfValuesToBeTested))))
  }
  "throw exception if ThresholdTwelveMonths == false, Exception Exists" in {
    val mapOfValuesToBeTested = List(
      s"$ThresholdInTwelveMonthsId" -> Json.obj("value" -> JsBoolean(false)),
      s"$ThresholdNextThirtyDaysId" -> JsBoolean(false),
      s"$ThresholdPreviousThirtyDaysId" -> Json.obj("value" -> JsBoolean(false)),
      s"$VoluntaryRegistrationId" -> JsBoolean(true),
      s"$VATExemptionId" -> JsBoolean(false),
      s"$ZeroRatedSalesId" -> JsBoolean(true),
      s"$VATRegistrationExceptionId" -> JsBoolean(false)
    )
    intercept[Exception](PageIdBinding.sectionBindings(new CacheMap("test", listMapWithoutFieldsToBeTested.++:(mapOfValuesToBeTested))))
  }
  "no exception if ThresholdTwelveMonths == false, Exception does not exist" in {
    val mapOfValuesToBeTested = List(
      s"$ThresholdInTwelveMonthsId" -> Json.obj("value" -> JsBoolean(false)),
      s"$ThresholdNextThirtyDaysId" -> Json.obj("value" -> JsBoolean(false)),
      s"$ThresholdPreviousThirtyDaysId" -> Json.obj("value" -> JsBoolean(false)),
      s"$VoluntaryRegistrationId" -> JsBoolean(true),
      s"$VATExemptionId" -> JsBoolean(false),
      s"$ZeroRatedSalesId" -> JsBoolean(true),
      s"$RegisteringBusinessId" -> JsBoolean(true),
      s"$NinoId" -> JsBoolean(true)

    )
    PageIdBinding.sectionBindings(new CacheMap("test", listMapWithoutFieldsToBeTested.++:(mapOfValuesToBeTested)))
  }
}