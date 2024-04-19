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

package helpers

import com.github.tomakehurst.wiremock.client.WireMock.{ok, patch, stubFor, urlMatching}
import identifiers._
import play.api.libs.json.{JsBoolean, Json}

trait VatRegistrationStub {

  val testEligibilityDataFull = Json.obj(
    ThresholdInTwelveMonthsId.toString -> Json.obj("value" -> JsBoolean(false)),
    ThresholdNextThirtyDaysId.toString -> Json.obj("value" -> JsBoolean(false)),
    ThresholdPreviousThirtyDaysId.toString -> Json.obj("value" -> JsBoolean(false)),
    VoluntaryRegistrationId.toString -> JsBoolean(true),
    InternationalActivitiesId.toString -> JsBoolean(false),
    VoluntaryRegistrationId.toString -> JsBoolean(true),
    RegisteringBusinessId.toString -> JsBoolean(true),
    AgriculturalFlatRateSchemeId.toString -> JsBoolean(false)
  )

  def stubSaveEligibilityData(regId: String) = {
    stubFor(
      patch(urlMatching(s"/vatreg/$regId/eligibility-data"))
        .willReturn(ok(testEligibilityDataFull.toString()))
    )
  }

}
