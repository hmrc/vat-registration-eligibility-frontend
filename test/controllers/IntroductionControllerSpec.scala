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

import controllers.actions.FakeCacheIdentifierAction
import play.api.test.Helpers._
import views.html.introduction

class IntroductionControllerSpec extends ControllerSpecBase {

  val view = app.injector.instanceOf[introduction]

  object Controller extends IntroductionController(
    controllerComponents,
    identify = FakeCacheIdentifierAction,
    view
  )

  def viewAsString = view()(fakeRequest, messages, frontendAppConfig).toString

  "onPageLoad" must {
    "return OK with the correct view" in {
      val res = Controller.onPageLoad(fakeRequest)
      status(res) mustBe OK
      contentAsString(res) mustBe viewAsString
    }
  }

  "onSubmit" must {
    "redirect to the FixedEstablishment controller" in {
      val res = Controller.onSubmit()(fakeRequest)

      status(res) mustBe SEE_OTHER
      redirectLocation(res) must contain(routes.FixedEstablishmentController.onPageLoad.url)
    }
  }
}
