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

import featureswitch.core.config.{FeatureSwitching, ThirdPartyTransactorFlow}
import helpers.{AuthHelper, IntegrationSpecBase, SessionStub}
import identifiers.{BusinessEntityId, RegisteringBusinessId}
import models.{BusinessEntity, NETP, Overseas, OwnBusiness, RegisteringBusiness, SomeoneElse}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Format._
import play.mvc.Http.HeaderNames

class RegisteringBusinessControllerISpec extends IntegrationSpecBase with AuthHelper with SessionStub with FeatureSwitching {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build()

  val internalId = "testInternalId"
  s"POST ${controllers.routes.RegisteringBusinessController.onSubmit.url}" should {

    "navigate to Registration Reason Page when own business" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      disable(ThirdPartyTransactorFlow)

      val request = buildClient(controllers.routes.RegisteringBusinessController.onSubmit.url)
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("own")))

      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.RegistrationReasonController.onPageLoad.url)
      verifySessionCacheData(internalId, RegisteringBusinessId.toString, Option.apply[RegisteringBusiness](OwnBusiness))
    }

    "navigate to Taxable Supplies for own business when flow is NETP" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      disable(ThirdPartyTransactorFlow)
      cacheSessionData[BusinessEntity](internalId, s"$BusinessEntityId", NETP)

      val request = buildClient(controllers.routes.RegisteringBusinessController.onSubmit.url)
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("own")))

      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaxableSuppliesInUkController.onPageLoad.url)
      verifySessionCacheData(internalId, RegisteringBusinessId.toString, Option.apply[RegisteringBusiness](OwnBusiness))
    }

    "navigate to Taxable Supplies for own business when flow is Overseas" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      disable(ThirdPartyTransactorFlow)
      cacheSessionData[BusinessEntity](internalId, s"$BusinessEntityId", Overseas)

      val request = buildClient(controllers.routes.RegisteringBusinessController.onSubmit.url)
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("own")))

      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaxableSuppliesInUkController.onPageLoad.url)
      verifySessionCacheData(internalId, RegisteringBusinessId.toString, Option.apply[RegisteringBusiness](OwnBusiness))
    }

    "navigate to Vat Exception Kickout when someone else's business" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      disable(ThirdPartyTransactorFlow)

      val request = buildClient(controllers.routes.RegisteringBusinessController.onSubmit.url)
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("someone-else")))

      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VATExceptionKickoutController.onPageLoad.url)
      verifySessionCacheData(internalId, RegisteringBusinessId.toString, Option.apply[RegisteringBusiness](SomeoneElse))
    }

    "navigate to Registration Reason Page when someone else's business if Third Party Transactor flow" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      enable(ThirdPartyTransactorFlow)

      val request = buildClient(controllers.routes.RegisteringBusinessController.onSubmit.url)
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("someone-else")))

      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.RegistrationReasonController.onPageLoad.url)
      verifySessionCacheData(internalId, RegisteringBusinessId.toString, Option.apply[RegisteringBusiness](SomeoneElse))
    }

    "navigate to Taxable Supplies when someone else's business if Third Party Transactor flow and NETP flow" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      enable(ThirdPartyTransactorFlow)
      cacheSessionData[BusinessEntity](internalId, s"$BusinessEntityId", NETP)

      val request = buildClient(controllers.routes.RegisteringBusinessController.onSubmit.url)
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("someone-else")))

      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaxableSuppliesInUkController.onPageLoad.url)
      verifySessionCacheData(internalId, RegisteringBusinessId.toString, Option.apply[RegisteringBusiness](SomeoneElse))
    }

    "navigate to Taxable Supplies when someone else's business if Third Party Transactor flow and Overseas flow" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      enable(ThirdPartyTransactorFlow)
      cacheSessionData[BusinessEntity](internalId, s"$BusinessEntityId", Overseas)

      val request = buildClient(controllers.routes.RegisteringBusinessController.onSubmit.url)
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("someone-else")))

      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaxableSuppliesInUkController.onPageLoad.url)
      verifySessionCacheData(internalId, RegisteringBusinessId.toString, Option.apply[RegisteringBusiness](SomeoneElse))
    }
  }
}