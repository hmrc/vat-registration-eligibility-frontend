/*
 * Copyright 2021 HM Revenue & Customs
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

import controllers.actions._
import play.api.mvc.Call
import play.api.test.Helpers._
import views.html.doNotNeedToRegister

class DoNotNeedToRegisterControllerSpec extends ControllerSpecBase {

  implicit val appConfig = frontendAppConfig

  val view = app.injector.instanceOf[doNotNeedToRegister]

  def onwardRoute: Call = routes.DoNotNeedToRegisterController.onPageLoad

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyCacheMap) =
    new DoNotNeedToRegisterController(controllerComponents, FakeCacheIdentifierAction, view)

  def viewAsString() = view()(fakeCacheDataRequestIncorped, messages, frontendAppConfig).toString

  val questionnaireUrl = frontendAppConfig.exitSurveyUrl

  "DoNotNeedToRegister Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody()

      val result = controller().onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(questionnaireUrl)
    }

  }
}



