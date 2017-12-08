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

import helpers.FormInspectors._
import helpers.VatRegSpec
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.http.Status.NO_CONTENT
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest

case class TestClass(text: String, number: Int)


class VatRegistrationControllerSpec extends VatRegSpec {

  class Setup{
    val testController = new VatRegistrationController {
      override def messagesApi = mockMessages
      override val authConnector = mockAuthConnector
      def authorisedActionGenerator: Action[AnyContent] = authorised { u => r => NoContent }
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

  "unauthorised access" should {
    "redirect user to GG sign in page" in new Setup {
      testController.authorisedActionGenerator(FakeRequest()) redirectsTo authUrl
    }
  }

  "authorised access" should {
    "return success status" in new Setup {
      callAuthorised(testController.authorisedActionGenerator)(status(_) shouldBe NO_CONTENT)
    }
  }
}
