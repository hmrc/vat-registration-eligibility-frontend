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

import play.api.mvc.Call
import play.api.test.Helpers._

class FeedbackControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = routes.IndexController.onPageLoad

  val controller: FeedbackController = new FeedbackController(frontendAppConfig, controllerComponents)

  val deskproId = "vrs"


  "VoluntaryRegistration Controller" must {

    "redirect to the feedback repo on a GET" in {
      val result = controller.show()(fakeRequest)

      status(result) mustBe SEE_OTHER

      redirectLocation(result).map { url =>
        url.contains("/contact/beta-feedback") mustBe true
        url.contains(s"service=$deskproId") mustBe true
      }
    }
  }
}