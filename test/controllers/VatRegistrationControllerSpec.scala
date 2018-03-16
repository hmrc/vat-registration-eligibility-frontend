/*
 * Copyright 2018 HM Revenue & Customs
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

import helpers.ControllerSpec
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import utils.InternalExceptions.{BRDocumentNotFound, VatFootprintNotFound}
import utils.SessionProfile

import scala.concurrent.Future

case class TestClass(text: String, number: Int)


class VatRegistrationControllerSpec extends ControllerSpec with GuiceOneAppPerTest {

  object TestController extends VatRegistrationController with SessionProfile {
    val messagesApi = fakeApplication.injector.instanceOf(classOf[MessagesApi])
    val authConnector = mockAuthClientConnector
    val currentProfileService = mockCurrentProfileService

    def callAuthenticated = isAuthenticated {
      implicit request =>
        Future.successful(Ok("ALL GOOD"))
    }

    def callAuthenticatedButError = isAuthenticated {
      implicit request =>
        Future.failed(new Exception("Something wrong"))
    }

    def callAuthenticatedWithProfile = isAuthenticatedWithProfile {
      _ => profile =>
        Future.successful(Ok(s"ALL GOOD with profile: ${profile.registrationId}"))
    }

    def callAuthenticatedWithProfileButError = isAuthenticatedWithProfile {
      _ => profile =>
        Future.failed(new Exception(s"Something wrong for profile: ${profile.registrationId}"))
    }
  }

  val testConstraint: Constraint[TestClass] = Constraint {
    case TestClass(t, n) if t.length < 5 && n > 20 => Invalid(ValidationError("message.code", "text"))
    case _ => Valid
  }

  val testForm = Form(
    mapping(
      "text" -> text(),
      "number" -> number()
    )(TestClass.apply)(TestClass.unapply).verifying(testConstraint)
  )

  "isAuthenticated" should {
    "return 200 if user is Authenticated" in {
      mockAuthenticated()

      val result = TestController.callAuthenticated(FakeRequest())
      status(result) mustBe OK
      contentAsString(result) mustBe "ALL GOOD"
    }

    "return 303 to GG login if user has No Active Session" in {
      mockNoActiveSession()

      val result = TestController.callAuthenticated(FakeRequest())
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("http://localhost:9025/gg/sign-in?accountType=organisation&continue=http%3A%2F%2Flocalhost%3A9894%2Fcheck-if-you-can-register-for-vat%2Fpost-sign-in&origin=vat-registration-eligibility-frontend")
    }

    "return 500 if user is Not Authenticated" in {
      mockNotAuthenticated()

      val result = TestController.callAuthenticated(FakeRequest())
      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "return an Exception if something went wrong" in {
      mockAuthenticated()

      val result = TestController.callAuthenticatedButError(FakeRequest())
      an[Exception] mustBe thrownBy(await(result))
    }
  }

  "isAuthenticatedWithProfile" should {
    "return 200 with a profile if user is Authenticated" in {
      mockAuthenticated()
      mockWithCurrentProfile(currentProfile)

      val result = TestController.callAuthenticatedWithProfile(FakeRequest())
      status(result) mustBe OK
      contentAsString(result) mustBe s"ALL GOOD with profile: ${currentProfile.registrationId}"
    }

    "return 303 to GG login if user has No Active Session" in {
      mockNoActiveSession()

      val result = TestController.callAuthenticatedWithProfile(FakeRequest())
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("http://localhost:9025/gg/sign-in?accountType=organisation&continue=http%3A%2F%2Flocalhost%3A9894%2Fcheck-if-you-can-register-for-vat%2Fpost-sign-in&origin=vat-registration-eligibility-frontend")
    }

    "redirect to startVat route" when {

      val startVatRedirect: String = "/check-if-you-can-register-for-vat/start-vat"

      "no BR document could be found" in {
        mockAuthenticated()
        when(mockCurrentProfileService.getCurrentProfile()(any())) thenReturn Future.failed(new BRDocumentNotFound(""))

        val result = TestController.callAuthenticatedWithProfile(FakeRequest())

        status(result) mustBe 303
        redirectLocation(result) mustBe Some(startVatRedirect)

      }
      "no vat footprint could be found" in {
        mockAuthenticated()
        when(mockCurrentProfileService.getCurrentProfile()(any())) thenReturn Future.failed(new VatFootprintNotFound(""))

        val result = TestController.callAuthenticatedWithProfile(FakeRequest())

        status(result) mustBe 303
        redirectLocation(result) mustBe Some(startVatRedirect)
      }
    }

    "return 500 if user is Not Authenticated" in {
      mockNotAuthenticated()

      val result = TestController.callAuthenticatedWithProfile(FakeRequest())
      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "return an Exception if something went wrong" in {
      mockAuthenticated()
      mockWithCurrentProfile(currentProfile)

      val result = TestController.callAuthenticatedWithProfileButError(FakeRequest())
      an[Exception] mustBe thrownBy(await(result))
    }
  }
}
