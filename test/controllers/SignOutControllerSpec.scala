/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.test.Helpers._

class SignOutControllerSpec extends ControllerSpecBase {

  def controller = new SignOutController(controllerComponents)

  val questionnaireUrl: String = frontendAppConfig.exitSurveyUrl

  "SignOutController" must {
    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody()

      val result = controller.signOut()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(questionnaireUrl)
    }
  }
}




