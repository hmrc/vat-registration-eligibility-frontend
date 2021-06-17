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

package www

import featureswitch.core.config.{FeatureSwitching, GeneralPartnershipFlow, SoleTraderFlow}
import helpers.{AuthHelper, IntegrationSpecBase, S4LStub, SessionStub}
import identifiers.{BusinessEntityId, InternationalActivitiesId}
import models._
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Format._
import play.mvc.Http.HeaderNames

class InternationalActivitiesControllerISpec extends IntegrationSpecBase with AuthHelper with SessionStub with FeatureSwitching with S4LStub {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build()

  val internalId = "testInternalId"

  s"POST ${controllers.routes.InternationalActivitiesController.onSubmit().url}" should {
    "navigate to International Activities dropout when true" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)

      cacheSessionData[BusinessEntity](internalId, s"$BusinessEntityId", UKCompany)

      val request = buildClient(controllers.routes.InternationalActivitiesController.onSubmit().url)
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("true")))

      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.EligibilityDropoutController.internationalActivitiesDropout().url)
      verifySessionCacheData(internalId, InternationalActivitiesId.toString, Option.apply[Boolean](true))
    }

    "navigate to Involved In Other Business when false and UKCompany" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)

      cacheSessionData[BusinessEntity](internalId, s"$BusinessEntityId", UKCompany)

      val request = buildClient(controllers.routes.InternationalActivitiesController.onSubmit().url)
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("false")))

      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.InvolvedInOtherBusinessController.onPageLoad().url)
      verifySessionCacheData(internalId, InternationalActivitiesId.toString, Option.apply[Boolean](false))
    }

    "navigate to Involved In Other Business when false and SoleTrader when FS is on" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)
      enable(SoleTraderFlow)

      cacheSessionData[BusinessEntity](internalId, s"$BusinessEntityId", SoleTrader)

      val request = buildClient(controllers.routes.InternationalActivitiesController.onSubmit().url)
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("false")))

      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.InvolvedInOtherBusinessController.onPageLoad().url)
      verifySessionCacheData(internalId, InternationalActivitiesId.toString, Option.apply[Boolean](false))
    }

    "navigate to Involved In Other Business when false and General Partnership when FS is on" in {
      stubSuccessfulLogin()
      stubSuccessfulRegIdGet()
      stubAudits()
      stubS4LGetNothing(testRegId)
      enable(GeneralPartnershipFlow)

      cacheSessionData[BusinessEntity](internalId, s"$BusinessEntityId", GeneralPartnership)

      val request = buildClient(controllers.routes.InternationalActivitiesController.onSubmit().url)
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
        .post(Map("value" -> Seq("false")))

      val response = await(request)
      response.status mustBe 303
      response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.InvolvedInOtherBusinessController.onPageLoad().url)
      verifySessionCacheData(internalId, InternationalActivitiesId.toString, Option.apply[Boolean](false))
    }

    "navigate to Vat Exception Kickout when false but business entity is not allowed" in {
      disable(GeneralPartnershipFlow)
      disable(SoleTraderFlow)

      val entityList = Seq(
        SoleTrader,
        GeneralPartnership,
        LimitedPartnership,
        ScottishPartnership,
        ScottishLimitedPartnership,
        LimitedLiabilityPartnership,
        CharitableIncorporatedOrganisation,
        NonIncorporatedTrust,
        RegisteredSociety,
        UnincorporatedAssociation,
        Division,
        VatGroup
      )

      entityList.map { entity =>
        stubSuccessfulLogin()
        stubSuccessfulRegIdGet()
        stubAudits()
        stubS4LGetNothing(testRegId)

        cacheSessionData[BusinessEntity](internalId, s"$BusinessEntityId", entity)

        val request = buildClient(controllers.routes.InternationalActivitiesController.onSubmit().url)
          .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(), "Csrf-Token" -> "nocheck")
          .post(Map("value" -> Seq("false")))

        val response = await(request)
        response.status mustBe 303
        response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VATExceptionKickoutController.onPageLoad().url)
        verifySessionCacheData(internalId, InternationalActivitiesId.toString, Option.apply[Boolean](false))
      }
    }
  }
}