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
import controllers.routes
import identifiers._
import models._
import play.api.libs.json.{JsBoolean, JsValue, Json}
import play.api.mvc.Call
import uk.gov.hmrc.http.cache.client.CacheMap

import java.time.LocalDate

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator
  val testId = "testId"
  def newCacheMap(map: Map[String, JsValue]) = new UserAnswers(CacheMap(testId, map))

  "Navigator" when {
    "in Normal mode" must {
      "go to Index from an identifier that doesn't exist in the route map" in {
        case object UnknownIdentifier extends Identifier
        navigator.nextPage(UnknownIdentifier, NormalMode)(mock[UserAnswers]) mustBe routes.IntroductionController.onPageLoad
      }
    }
  }

  "pageIdToPageLoad" should {
    "load a page" when {
      Seq[(Identifier, Call)](

      ) foreach { case (id, page) =>
        s"given an ID of ${id.toString} should go to ${page.url}" in {
          navigator.pageIdToPageLoad(id).url must include(page.url)
        }
      }
    }

    "redirect to the start of the VAT EL Flow" when {
      "given an invalid ID" in {
        val fakeId = new Identifier {
          override def toString: String = "fudge"
        }
        navigator.pageIdToPageLoad(fakeId).url mustBe routes.IntroductionController.onPageLoad.url
      }
    }
  }

  "next on" should {
    "return and id and call" when {
      "true is passed in" in {
        val data = new UserAnswers(CacheMap("some-id", Map(ZeroRatedSalesId.toString -> JsBoolean(true))))
        val result = navigator.nextOn(true, ZeroRatedSalesId, AgriculturalFlatRateSchemeId, EligibilityDropoutId("mode"))
        result._2(data) mustBe controllers.routes.AgriculturalFlatRateSchemeController.onPageLoad
      }
    }
  }

  "nextOnZeroRateSales" should {
    "redirect to Mandatory Registration page (zero rated is false)" when {
      "user is mandatory" in {
        val data = newCacheMap(Map(
          ZeroRatedSalesId.toString -> JsBoolean(false),
          ThresholdNextThirtyDaysId.toString -> Json.toJson(ConditionalDateFormElement(value = true, Some(LocalDate.now())))
        ))
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.MandatoryInformationController.onPageLoad
      }

      "user is overseas and gone over threshold" in {
        val data = newCacheMap(Map(
          ZeroRatedSalesId.toString -> JsBoolean(false),
          GoneOverThresholdId.toString -> JsBoolean(true)
        ))
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.MandatoryInformationController.onPageLoad
      }

      "reg reason is VAT Group" in {
        val data = newCacheMap(Map(
          ZeroRatedSalesId.toString -> JsBoolean(false),
          RegistrationReasonId.toString -> Json.toJson(SettingUpVatGroup)
        ))
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.MandatoryInformationController.onPageLoad
      }

      "reg reason is TOGC" in {
        val data = newCacheMap(Map(
          ZeroRatedSalesId.toString -> JsBoolean(false),
          RegistrationReasonId.toString -> Json.toJson(TakingOverBusiness)
        ))
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.MandatoryInformationController.onPageLoad
      }

      "reg reason is COLE" in {
        val data = newCacheMap(Map(
          ZeroRatedSalesId.toString -> JsBoolean(false),
          RegistrationReasonId.toString -> Json.toJson(ChangingLegalEntityOfBusiness)
        ))
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.MandatoryInformationController.onPageLoad
      }
    }
    
    "redirect to Mandatory Registration page and skip Exemption page (zero rated is true)" when {
      "user is mandatory but exception is true" in {
        val data = newCacheMap(Map(
          ZeroRatedSalesId.toString -> JsBoolean(true),
          VATRegistrationExceptionId.toString -> JsBoolean(true),
          ThresholdInTwelveMonthsId.toString -> Json.toJson(ConditionalDateFormElement(value = true, Some(LocalDate.now())))
        ))
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.MandatoryInformationController.onPageLoad
      }

      "reg reason is VAT Group" in {
        val data = newCacheMap(Map(
          ZeroRatedSalesId.toString -> JsBoolean(true),
          RegistrationReasonId.toString -> Json.toJson(SettingUpVatGroup)
        ))
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.MandatoryInformationController.onPageLoad
      }
    }

    "redirect to Exemption page (zero rated is true)" when {
      "user is mandatory" in {
        val data = newCacheMap(Map(
          ZeroRatedSalesId.toString -> JsBoolean(true),
          ThresholdInTwelveMonthsId.toString -> Json.toJson(ConditionalDateFormElement(value = true, Some(LocalDate.now())))
        ))
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.VATExemptionController.onPageLoad
      }

      "user is overseas and gone over threshold" in {
        val data = newCacheMap(Map(
          ZeroRatedSalesId.toString -> JsBoolean(true),
          BusinessEntityId.toString -> Json.toJson(Overseas),
          GoneOverThresholdId.toString -> JsBoolean(true)
        ))
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.VATExemptionController.onPageLoad
      }

      "user is overseas and not gone over threshold" in {
        val data = newCacheMap(Map(
          ZeroRatedSalesId.toString -> JsBoolean(true),
          BusinessEntityId.toString -> Json.toJson(NETP),
          GoneOverThresholdId.toString -> JsBoolean(false)
        ))
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.VATExemptionController.onPageLoad
      }

      "reg reason is TOGC" in {
        val data = newCacheMap(Map(
          ZeroRatedSalesId.toString -> JsBoolean(true),
          RegistrationReasonId.toString -> Json.toJson(TakingOverBusiness)
        ))
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.VATExemptionController.onPageLoad
      }

      "reg reason is COLE" in {
        val data = newCacheMap(Map(
          ZeroRatedSalesId.toString -> JsBoolean(true),
          RegistrationReasonId.toString -> Json.toJson(ChangingLegalEntityOfBusiness)
        ))
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.VATExemptionController.onPageLoad
      }
    }

    "redirect to Voluntary Registration page" when {
      "zero rated is true and user is not mandatory" in {
        val data = newCacheMap(Map(ZeroRatedSalesId.toString -> JsBoolean(true)))
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.VoluntaryInformationController.onPageLoad
      }

      "zero rated is false and user is not mandatory" in {
        val data = newCacheMap(Map(ZeroRatedSalesId.toString -> JsBoolean(false)))
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.VoluntaryInformationController.onPageLoad
      }

      "zero rates is true, overseas user below threshold" in {
        val data = newCacheMap(Map(
          ZeroRatedSalesId.toString -> JsBoolean(true),
          GoneOverThresholdId.toString -> JsBoolean(false)
        ))
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.VoluntaryInformationController.onPageLoad
      }

      "zero rates is false, overseas user below threshold" in {
        val data = newCacheMap(Map(
          ZeroRatedSalesId.toString -> JsBoolean(false),
          GoneOverThresholdId.toString -> JsBoolean(false)
        ))
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.VoluntaryInformationController.onPageLoad
      }
    }

    "redirect to Zero Rated Sales page" when {
      "zero rated is not answered" in {
        val data = newCacheMap(Map())
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.ZeroRatedSalesController.onPageLoad
      }
    }
  }
}
