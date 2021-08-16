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

package controllers

import controllers.Assets.{IM_A_TEAPOT, OK}
import helpers.{AuthHelper, IntegrationSpecBase}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.mvc.Http.HeaderNames

class BusinessEntityOverseasControllerISpec extends IntegrationSpecBase with AuthHelper {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build()

  "GET /business-entity-overseas" must {
    "return OK" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()

      val res = await(buildClient(controllers.routes.BusinessEntityOverseasController.onPageLoad().url).get)

      res.status mustBe OK
    }
  }

  s"POST /business-entity-overseas" should {
    "return IM_A_TEAPOT" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()

      val request = buildClient(controllers.routes.BusinessEntityOverseasController.onSubmit().url)
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("true")))

      val response = await(request)
      response.status mustBe IM_A_TEAPOT
    }
  }
}