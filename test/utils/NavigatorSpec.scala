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
import play.api.libs.json.{JsBoolean, Json}
import play.api.mvc.Call
import uk.gov.hmrc.http.cache.client.CacheMap

import java.time.LocalDate

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

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
    "Skip Exemption" when {
      "Exception is true and yes is answered" in {
        val data = new UserAnswers(CacheMap("some-id", Map(ZeroRatedSalesId.toString -> JsBoolean(true), VATRegistrationExceptionId.toString -> JsBoolean(true))))
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.MandatoryInformationController.onPageLoad
      }
    }
    "Redirect to Exemption" when {
      "Not Voluntary Registration and yes is answered" in {
        val data = new UserAnswers(CacheMap("some-id", Map(ZeroRatedSalesId.toString -> JsBoolean(true))))
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.VATExemptionController.onPageLoad
      }
    }
    "Redirect to Voluntary" when {
      "Is Voluntary Registration and yes is answered" in {
        val testDate = LocalDate.of(1999, 12, 12)
        val data = new UserAnswers(CacheMap("some-id", Map(ZeroRatedSalesId.toString -> JsBoolean(true), ThresholdNextThirtyDaysId.toString -> Json.toJson(ConditionalDateFormElement(false, Some(testDate))))))
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.VoluntaryInformationController.onPageLoad
      }
    }
    "Redirect to Voluntary" when {
      "Is Voluntary Registration and no is answered" in {
        val data = new UserAnswers(CacheMap("some-id", Map(ZeroRatedSalesId.toString -> JsBoolean(false), VoluntaryRegistrationId.toString -> JsBoolean(true))))
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.VoluntaryInformationController.onPageLoad
      }
      "goneOverThreshold is false and zeroRatedSales is true" in {
        val data = new UserAnswers(CacheMap("some-id", Map(ZeroRatedSalesId.toString -> JsBoolean(true), GoneOverThresholdId.toString -> JsBoolean(false))))
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.VoluntaryInformationController.onPageLoad
      }
      "goneOverThreshold is false and zeroRatedSales is false" in {
        val testDate = LocalDate.of(1999, 12, 12)
        val data = new UserAnswers(CacheMap("some-id", Map(ZeroRatedSalesId.toString -> JsBoolean(false),ThresholdInTwelveMonthsId.toString -> Json.toJson(ConditionalDateFormElement(true, Some(testDate))) , GoneOverThresholdId.toString -> JsBoolean(false))))
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.VoluntaryInformationController.onPageLoad
      }
    }
    "Redirect to Mandatory" when {
      "Is Mandatory Registration and no is answered" in {
        val testDate = LocalDate.of(1999, 12, 12)
        val data = new UserAnswers(CacheMap("some-id", Map(ZeroRatedSalesId.toString -> JsBoolean(false), ThresholdInTwelveMonthsId.toString -> Json.toJson(ConditionalDateFormElement(true, Some(testDate))))))
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.MandatoryInformationController.onPageLoad
      }
      "goneOverThreshold is true and zeroRatedSales is false" in {
        val data = new UserAnswers(CacheMap("some-id", Map(ZeroRatedSalesId.toString -> JsBoolean(false), GoneOverThresholdId.toString -> JsBoolean(true))))
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.MandatoryInformationController.onPageLoad
      }
      "goneOverThreshold is true and zeroRatedSales is true" in {
        val data = new UserAnswers(CacheMap("some-id", Map(ZeroRatedSalesId.toString -> JsBoolean(true), GoneOverThresholdId.toString -> JsBoolean(true))))
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.MandatoryInformationController.onPageLoad
      }
      "goneOverThreshold is true with other conditions" in {
        val testDate = LocalDate.of(1999, 12, 12)
        val data = new UserAnswers(CacheMap("some-id", Map(ZeroRatedSalesId.toString -> JsBoolean(true), ThresholdNextThirtyDaysId.toString -> Json.toJson(ConditionalDateFormElement(false, Some(testDate))), GoneOverThresholdId.toString -> JsBoolean(true))))
        val result = navigator.nextOnZeroRateSales
        result._2(data) mustBe controllers.routes.MandatoryInformationController.onPageLoad
      }

    }
  }
}
