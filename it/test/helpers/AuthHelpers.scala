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
import play.api.http.HeaderNames
import play.api.libs.json.{JsArray, Json}

trait AuthHelper {
  self: IntegrationSpecBase =>

  def stubSuccessfulLogin(userId: String = defaultUser, withSignIn: Boolean = false, enrolments: JsArray = Json.arr()) = {
    if (withSignIn) {
      val continueUrl = "/wibble"
      stubFor(get(urlEqualTo(s"/gg/sign-in?continue=${continueUrl}"))
        .willReturn(aResponse()
          .withStatus(303)
          .withHeader(HeaderNames.SET_COOKIE, self.getSessionCookie())
          .withHeader(HeaderNames.LOCATION, continueUrl)))
    }

    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(ok(Json.obj(
          "optionalCredentials" -> Json.obj(
            "providerId" -> "testProviderID",
            "providerType" -> "GovernmentGateway"
          ),
          "internalId" -> testInternalId,
          "allEnrolments" -> enrolments
        ).toString()).withHeader(HeaderNames.SET_COOKIE, self.getSessionCookie())))
  }

  def stubUnauthorised() = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(unauthorized()))
  }

  def stubAudits() = {
    stubFor(post(urlMatching("/write/audit"))
      .willReturn(
        aResponse().
          withStatus(204)
      )
    )

    stubFor(post(urlMatching("/write/audit/merged"))
      .willReturn(
        aResponse().
          withStatus(204)
      )
    )
  }

}