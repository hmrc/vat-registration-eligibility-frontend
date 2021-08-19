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

package controllers

import featureswitch.core.config.{EnableAAS, FeatureSwitching}
import helpers.{AuthHelper, IntegrationSpecBase, SessionStub}
import identifiers.RacehorsesId
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Format._
import play.mvc.Http.HeaderNames

class RacehorsesISpec extends IntegrationSpecBase with AuthHelper with SessionStub with FeatureSwitching {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build()

  val internalId = "testInternalId"

  s"POST ${controllers.routes.RacehorsesController.onSubmit().url}" should {
    "navigate to Registering Business when false and EnableAAS is off" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      disable(EnableAAS)

      val request = buildClient(controllers.routes.RacehorsesController.onSubmit().url)
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("false")))

      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.AnnualAccountingSchemeController.onPageLoad.url)
      verifySessionCacheData(internalId, RacehorsesId.toString, Option.apply[Boolean](false))
    }

    "navigate to Registering Business when false and EnableAAS is on" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      enable(EnableAAS)

      val request = buildClient(controllers.routes.RacehorsesController.onSubmit().url)
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("false")))

      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.RegisteringBusinessController.onPageLoad.url)
      verifySessionCacheData(internalId, RacehorsesId.toString, Option.apply[Boolean](false))
    }

    "navigate to VAT Exception when true" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()

      val request = buildClient(controllers.routes.RacehorsesController.onSubmit().url)
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("true")))

      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VATExceptionKickoutController.onPageLoad.url)
      verifySessionCacheData(internalId, RacehorsesId.toString, Option.apply[Boolean](true))
    }
  }
}