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

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap

trait S4LStub {

  def s4LGetUrl(regId: String) = s"/save4later/vat-registration-eligibility-frontend/$regId"

  def s4LPutUrl(regId: String, key: String) = s"/save4later/vat-registration-eligibility-frontend/$regId/data/$key"

  def stubS4LGet(regId: String)(response: CacheMap): StubMapping =
    stubFor(get(urlMatching(s4LGetUrl(regId)))
      .willReturn(ok(Json.toJson(response).toString)))

  def stubS4LGetNothing(regId: String): StubMapping =
    stubFor(get(urlMatching(s4LGetUrl(regId)))
      .willReturn(aResponse().withStatus(NOT_FOUND)))

  def stubS4LSave[T](regId: String, key: String)(response: CacheMap): StubMapping =
    stubFor(put(urlMatching(s4LPutUrl(regId, key)))
      .willReturn(ok(Json.toJson(response).toString)))

}
