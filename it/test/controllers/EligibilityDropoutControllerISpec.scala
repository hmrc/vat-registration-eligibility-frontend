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

package controllers

import helpers.IntegrationSpecBase
import identifiers.{AgriculturalFlatRateSchemeId, BusinessEntityId}
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames
import views.html.{AgriculturalDropout, InternationalActivityDropout, VatDivisionDropout}

class EligibilityDropoutControllerISpec extends IntegrationSpecBase {

  val pageUrl = "/cant-register"
  val internationalActivitiesUrl = "/errors/business-activities-next-12-months"

  def cantRegisterUrl(id: String) = s"$pageUrl/$id"

  "GET /cant-register/agriculturalFlatRateScheme" must {
    "return OK" in new Setup {
      stubSuccessfulLogin()
      stubAudits()

      val view = app.injector.instanceOf[AgriculturalDropout]
      val res = await(buildClient(cantRegisterUrl(AgriculturalFlatRateSchemeId.toString)).get)

      res.status mustBe OK
      res.body mustBe view()(request(cantRegisterUrl(AgriculturalFlatRateSchemeId.toString)), messages, appConfig).toString()
    }
  }

  "GET /cant-register/businessEntity" must {
    "return OK" in new Setup {
      stubSuccessfulLogin()
      stubAudits()

      val view = app.injector.instanceOf[VatDivisionDropout]
      val res = await(buildClient(cantRegisterUrl(BusinessEntityId.toString)).get)

      res.status mustBe OK
      res.body mustBe view()(request(cantRegisterUrl(BusinessEntityId.toString)), messages, appConfig).toString()
    }
  }

  "GET /errors/business-activities-next-12-month" must {
    "return OK" in new Setup {
      stubSuccessfulLogin()
      stubAudits()

      val view = app.injector.instanceOf[InternationalActivityDropout]
      val res = await(buildClient(internationalActivitiesUrl).get)

      res.status mustBe OK
      res.body mustBe view()(request(internationalActivitiesUrl), messages, appConfig).toString()
    }
  }

}
