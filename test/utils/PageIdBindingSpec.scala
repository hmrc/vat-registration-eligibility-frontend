/*
 * Copyright 2023 HM Revenue & Customs
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

import featureswitch.core.config.FeatureSwitching
import identifiers._
import models._
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsBoolean, JsString, JsValue, Json}
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.collection.immutable.ListMap

class PageIdBindingSpec extends PlaySpec with FeatureSwitching {
  val fullListMapHappyPathTwelveMonthsFalse: ListMap[String, JsValue] = ListMap[String, JsValue](
    "" -> JsString(""),
    s"$FixedEstablishmentId" -> JsBoolean(true),
    s"$BusinessEntityId" -> Json.toJson(UKCompany),
    s"$ThresholdInTwelveMonthsId" -> Json.obj("value" -> JsBoolean(false)),
    s"$ThresholdNextThirtyDaysId" -> Json.obj("value" -> JsBoolean(false)),
    s"$VoluntaryRegistrationId" -> JsBoolean(true),
    s"$InternationalActivitiesId" -> JsBoolean(false),
    s"$RegisteringBusinessId" -> Json.toJson(OwnBusiness),
    s"$RegistrationReasonId" -> Json.toJson(SellingGoodsAndServices),
    s"$VoluntaryRegistrationId" -> JsBoolean(true),
    s"$VATRegistrationExceptionId" -> JsBoolean(false),
    s"$AgriculturalFlatRateSchemeId" -> JsBoolean(false)
  )

  val fullListMapHappyPathNETP: ListMap[String, JsValue] = ListMap[String, JsValue](
    "" -> JsString(""),
    s"$FixedEstablishmentId" -> JsBoolean(true),
    s"$BusinessEntityId" -> Json.toJson(NETP),
    s"$TaxableSuppliesInUkId" -> JsBoolean(true),
    s"$ThresholdTaxableSuppliesId" -> Json.obj("date" -> JsString("2020-12-12")),
    s"$VoluntaryRegistrationId" -> JsBoolean(true),
    s"$InternationalActivitiesId" -> JsBoolean(false),
    s"$RegisteringBusinessId" -> Json.toJson(OwnBusiness),
    s"$AgriculturalFlatRateSchemeId" -> JsBoolean(false)
  )

  fullListMapHappyPathTwelveMonthsFalse.foldLeft(Map[String, JsValue]()) {
    case (mockedReturn, currentItem) =>
      s"an exception should be experienced when only pages before ${currentItem._1} have been filled" in {
        intercept[Exception](PageIdBinding.sectionBindings(new CacheMap("testId", mockedReturn)))
      }
      mockedReturn + currentItem
  }
  val listMapWithoutFieldsToBeTested: Map[String, JsValue] = fullListMapHappyPathTwelveMonthsFalse.filterNot { s =>
    s._1 match {
      case x if x == ThresholdInTwelveMonthsId.toString || x == ThresholdNextThirtyDaysId.toString ||
        x == ThresholdPreviousThirtyDaysId.toString || x == VoluntaryRegistrationId.toString ||
        x == VATRegistrationExceptionId.toString => true
      case _ => false
    }
  }

  val listMapWithoutFieldsToBeTestedNETP: Map[String, JsValue] = fullListMapHappyPathNETP.filterNot { s =>
    s._1 match {
      case x if x == ThresholdInTwelveMonthsId.toString || x == ThresholdNextThirtyDaysId.toString ||
        x == ThresholdPreviousThirtyDaysId.toString || x == VoluntaryRegistrationId.toString ||
        x == VATRegistrationExceptionId.toString => true
      case _ => false
    }
  }

  "no exception should be thrown when a cacheMap containing ThresholdTwelveMonths == true, ThresholdNextThirty doesn't exist" in {
    val mapOfValuesToBeTested = List(
      s"$FixedEstablishmentId" -> JsBoolean(true),
      s"$BusinessEntityId" -> Json.toJson(UKCompany),
      s"$ThresholdInTwelveMonthsId" -> Json.obj("value" -> JsBoolean(true)),
      s"$ThresholdPreviousThirtyDaysId" -> Json.obj("value" -> JsBoolean(false)),
      s"$RegisteringBusinessId" -> Json.toJson(OwnBusiness),
      s"$VATRegistrationExceptionId" -> JsBoolean(false)
    )
    PageIdBinding.sectionBindings(new CacheMap("test", listMapWithoutFieldsToBeTested.++:(mapOfValuesToBeTested)))
  }
  "exception should be thrown when a cacheMap containing ThresholdTwelveMonths == true, ThresholdNextThirty does exist" in {
    val mapOfValuesToBeTested = List(
      s"$FixedEstablishmentId" -> JsBoolean(true),
      s"$BusinessEntityId" -> Json.toJson(UKCompany),
      s"$ThresholdInTwelveMonthsId" -> Json.obj("value" -> JsBoolean(true)),
      s"$ThresholdNextThirtyDaysId" -> JsBoolean(true),
      s"$ThresholdPreviousThirtyDaysId" -> Json.obj("value" -> JsBoolean(false)),
      s"$VATRegistrationExceptionId" -> JsBoolean(false)
    )
    intercept[Exception](PageIdBinding.sectionBindings(new CacheMap("test", listMapWithoutFieldsToBeTested.++:(mapOfValuesToBeTested))))
  }
  "throw new exception if voluntary flag does not exist when all 3 threshold q's are no" in {
    val mapOfValuesToBeTested = List(
      s"$ThresholdInTwelveMonthsId" -> Json.obj("value" -> JsBoolean(false)),
      s"$ThresholdNextThirtyDaysId" -> JsBoolean(false)
    )
    intercept[Exception](PageIdBinding.sectionBindings(new CacheMap("test", listMapWithoutFieldsToBeTested.++:(mapOfValuesToBeTested))))
  }
  "throw exception if all 3 threshold q's exist, one answer == true, voluntary flag exists" in {
    val mapOfValuesToBeTested = List(
      s"$ThresholdInTwelveMonthsId" -> Json.obj("value" -> JsBoolean(true)),
      s"$ThresholdNextThirtyDaysId" -> JsBoolean(false),
      s"$ThresholdPreviousThirtyDaysId" -> Json.obj("value" -> JsBoolean(false)),
      s"$VoluntaryRegistrationId" -> JsBoolean(true),
      s"$VATRegistrationExceptionId" -> JsBoolean(false)
    )
    intercept[Exception](PageIdBinding.sectionBindings(new CacheMap("test", listMapWithoutFieldsToBeTested.++:(mapOfValuesToBeTested))))
  }
  "throw exception if thresholdInTwelveMonths is false and thresholdInPreviousThirtyDays is true" in {
    val mapOfValuesToBeTested = List(
      s"$ThresholdInTwelveMonthsId" -> Json.obj("value" -> JsBoolean(false)),
      s"$ThresholdPreviousThirtyDaysId" -> Json.obj("value" -> JsBoolean(false)),
      s"$VoluntaryRegistrationId" -> JsBoolean(false),
      s"$VATRegistrationExceptionId" -> JsBoolean(false)
    )
    intercept[Exception](PageIdBinding.sectionBindings(new CacheMap("test", listMapWithoutFieldsToBeTested.++:(mapOfValuesToBeTested))))
  }
  "throw exception if thresholdInTwelveMonths is true and thresholdInPreviousThirtyDays does not exist" in {
    val mapOfValuesToBeTested = List(
      s"$ThresholdInTwelveMonthsId" -> Json.obj("value" -> JsBoolean(false)),
      s"$VoluntaryRegistrationId" -> JsBoolean(true),
      s"$VATRegistrationExceptionId" -> JsBoolean(false)
    )
    intercept[Exception](PageIdBinding.sectionBindings(new CacheMap("test", listMapWithoutFieldsToBeTested.++:(mapOfValuesToBeTested))))
  }
  "throw exception if thresholdInPreviousThirtyDays exists but thresholdInTwelveMonths does not exist" in {
    val mapOfValuesToBeTested = List(
      s"$ThresholdPreviousThirtyDaysId" -> Json.obj("value" -> JsBoolean(false)),
      s"$VoluntaryRegistrationId" -> JsBoolean(false),
      s"$VATRegistrationExceptionId" -> JsBoolean(false)
    )
    intercept[Exception](PageIdBinding.sectionBindings(new CacheMap("test", listMapWithoutFieldsToBeTested.++:(mapOfValuesToBeTested))))
  }
  "throw exception if ThresholdTwelveMonths == false, Exception Exists" in {
    val mapOfValuesToBeTested = List(
      s"$ThresholdInTwelveMonthsId" -> Json.obj("value" -> JsBoolean(false)),
      s"$ThresholdNextThirtyDaysId" -> JsBoolean(false),
      s"$VoluntaryRegistrationId" -> JsBoolean(true),
      s"$VATRegistrationExceptionId" -> JsBoolean(false)
    )
    intercept[Exception](PageIdBinding.sectionBindings(new CacheMap("test", listMapWithoutFieldsToBeTested.++:(mapOfValuesToBeTested))))
  }
  "no exception if ThresholdTwelveMonths == false, Exception does not exist" in {
    val mapOfValuesToBeTested = List(
      s"$FixedEstablishmentId" -> JsBoolean(true),
      s"$BusinessEntityId" -> Json.toJson(UKCompany),
      s"$ThresholdInTwelveMonthsId" -> Json.obj("value" -> JsBoolean(false)),
      s"$ThresholdNextThirtyDaysId" -> Json.obj("value" -> JsBoolean(false)),
      s"$VoluntaryRegistrationId" -> JsBoolean(true),
      s"$RegisteringBusinessId" -> Json.toJson(OwnBusiness)
    )
    PageIdBinding.sectionBindings(new CacheMap("test", listMapWithoutFieldsToBeTested.++:(mapOfValuesToBeTested)))
  }

  "no exception if TaxableSuppliesInUk doesn't exist, when UK Company" in {
    val mapOfValuesToBeTested = List(
      s"$FixedEstablishmentId" -> JsBoolean(true),
      s"$BusinessEntityId" -> Json.toJson(UKCompany),
      s"$ThresholdInTwelveMonthsId" -> Json.obj("value" -> JsBoolean(false)),
      s"$ThresholdNextThirtyDaysId" -> Json.obj("value" -> JsBoolean(false)),
      s"$VoluntaryRegistrationId" -> JsBoolean(true),
      s"$RegisteringBusinessId" -> Json.toJson(OwnBusiness)
    )
    PageIdBinding.sectionBindings(new CacheMap("test", listMapWithoutFieldsToBeTested.++:(mapOfValuesToBeTested).-(s"$TaxableSuppliesInUkId")))
  }

  "no exception if ThresholdTaxableSupplies doesn't exist, when UK Company" in {
    val mapOfValuesToBeTested = List(
      s"$FixedEstablishmentId" -> JsBoolean(true),
      s"$BusinessEntityId" -> Json.toJson(UKCompany),
      s"$ThresholdInTwelveMonthsId" -> Json.obj("value" -> JsBoolean(false)),
      s"$ThresholdNextThirtyDaysId" -> Json.obj("value" -> JsBoolean(false)),
      s"$VoluntaryRegistrationId" -> JsBoolean(true),
      s"$RegisteringBusinessId" -> Json.toJson(OwnBusiness)
    )
    PageIdBinding.sectionBindings(new CacheMap("test", listMapWithoutFieldsToBeTested.++:(mapOfValuesToBeTested).-(s"$ThresholdTaxableSuppliesId")))
  }

  "no exception if GoneOverThreshold doesn't exist, when UK Company" in {
    val mapOfValuesToBeTested = List(
      s"$FixedEstablishmentId" -> JsBoolean(true),
      s"$BusinessEntityId" -> Json.toJson(UKCompany),
      s"$ThresholdInTwelveMonthsId" -> Json.obj("value" -> JsBoolean(false)),
      s"$ThresholdNextThirtyDaysId" -> Json.obj("value" -> JsBoolean(false)),
      s"$VoluntaryRegistrationId" -> JsBoolean(true),
      s"$RegisteringBusinessId" -> Json.toJson(OwnBusiness)
    )
    PageIdBinding.sectionBindings(new CacheMap("test", listMapWithoutFieldsToBeTested.++:(mapOfValuesToBeTested)))
  }

  "throw exception if TaxableSuppliesInUk answer doesn't exist when NETP" in {
    intercept[NoSuchElementException](PageIdBinding.sectionBindings(
      new CacheMap("test", fullListMapHappyPathNETP.-(s"$TaxableSuppliesInUkId")))
    )
  }

  "throw exception if ThresholdTaxableSupplies answer doesn't exist when NETP" in {
    intercept[NoSuchElementException](PageIdBinding.sectionBindings(
      new CacheMap("test", fullListMapHappyPathNETP.-(s"$ThresholdTaxableSuppliesId")))
    )
  }

  "no exception on Full Happy Path, when the user is a NETP" in {
    val mapOfValuesToBeTested = List()
    PageIdBinding.sectionBindings(new CacheMap("test", listMapWithoutFieldsToBeTestedNETP.++:(mapOfValuesToBeTested)))
  }

  "no exception if ThresholdNextThirtyDays is missing, when the user is a NETP" in {
    val mapOfValuesToBeTested = List(
      s"$FixedEstablishmentId" -> JsBoolean(true),
      s"$BusinessEntityId" -> Json.toJson(NETP),
      s"$ThresholdInTwelveMonthsId" -> Json.obj("value" -> JsBoolean(false)),
      s"$VoluntaryRegistrationId" -> JsBoolean(true),
      s"$RegisteringBusinessId" -> Json.toJson(OwnBusiness)
    )
    PageIdBinding.sectionBindings(new CacheMap("test", listMapWithoutFieldsToBeTestedNETP.++:(mapOfValuesToBeTested).-(s"$ThresholdNextThirtyDaysId")))
  }

  "no exception if ThresholdInTwelveMonths is missing, when the user is a NETP" in {
    val mapOfValuesToBeTested = List(
      s"$FixedEstablishmentId" -> JsBoolean(true),
      s"$BusinessEntityId" -> Json.toJson(NETP),
      s"$ThresholdNextThirtyDaysId" -> Json.obj("value" -> JsBoolean(false)),
      s"$VoluntaryRegistrationId" -> JsBoolean(true),
      s"$RegisteringBusinessId" -> Json.toJson(OwnBusiness)
    )
    PageIdBinding.sectionBindings(new CacheMap("test", listMapWithoutFieldsToBeTestedNETP.++:(mapOfValuesToBeTested).-(s"$ThresholdInTwelveMonthsId")))
  }

  "no exception if ThresholdPreviousThirtyDays is missing, when the user is a NETP" in {
    val mapOfValuesToBeTested = List(
      s"$FixedEstablishmentId" -> JsBoolean(true),
      s"$BusinessEntityId" -> Json.toJson(NETP),
      s"$VoluntaryRegistrationId" -> JsBoolean(true),
      s"$RegisteringBusinessId" -> Json.toJson(OwnBusiness)
    )
    PageIdBinding.sectionBindings(new CacheMap("test", listMapWithoutFieldsToBeTestedNETP.++:(mapOfValuesToBeTested).-(s"$ThresholdPreviousThirtyDaysId")))
  }

  "PageIdBinding for a user on TOGC/COLE flow" must {
    val testPreviousName = "testPreviousName"
    val testVrn = "testVrn"

    def fullListMap(partyType: BusinessEntity, regReason: RegistrationReason): ListMap[String, JsValue] = ListMap[String, JsValue](
      s"$FixedEstablishmentId" -> JsBoolean(true),
      s"$BusinessEntityId" -> Json.toJson(partyType),
      s"$InternationalActivitiesId" -> JsBoolean(false),
      s"$RegisteringBusinessId" -> Json.toJson(OwnBusiness),
      s"$RegistrationReasonId" -> Json.toJson(regReason),
      s"$VATRegistrationExceptionId" -> JsBoolean(false),
      s"$AgriculturalFlatRateSchemeId" -> JsBoolean(false),
      s"$DateOfBusinessTransferId" -> Json.obj("date" -> JsString("2020-12-12")),
      s"$PreviousBusinessNameId" -> JsString(testPreviousName),
      s"$VATNumberId" -> JsString(testVrn),
      s"$KeepOldVrnId" -> JsBoolean(true),
      s"$TermsAndConditionsId" -> JsBoolean(true)
    )

    List(TakingOverBusiness, ChangingLegalEntityOfBusiness).foreach { reason =>
      s"pass Full Happy Path, when the user is UkCompany on $reason flow" in {
        PageIdBinding.sectionBindings(new CacheMap("test", fullListMap(UKCompany, reason)))
      }

      s"pass Full Happy Path, when the user is NETP on $reason flow" in {
        PageIdBinding.sectionBindings(new CacheMap("test", fullListMap(NETP, reason)))
      }
    }
  }

}