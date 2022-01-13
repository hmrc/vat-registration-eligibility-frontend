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

import controllers.actions._
import featureswitch.core.config.{FeatureSwitching, VATGroupFlow}
import forms.InvolvedInOtherBusinessFormProvider
import models.NormalMode
import play.api.data.Form
import play.api.test.Helpers._
import services.FakeSessionService
import utils.FakeNavigator
import views.html.involvedInOtherBusiness

class InvolvedInOtherBusinessControllerSpec extends ControllerSpecBase with FeatureSwitching {

  def onwardRoute = routes.IndexController.onPageLoad

  val view = app.injector.instanceOf[involvedInOtherBusiness]

  val formProvider = new InvolvedInOtherBusinessFormProvider()
  val form = formProvider.form
  implicit val appConfig = frontendAppConfig

  val dataRequiredAction = new DataRequiredAction

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyCacheMap) =
    new InvolvedInOtherBusinessController(controllerComponents, FakeSessionService, new FakeNavigator(desiredRoute = onwardRoute), FakeCacheIdentifierAction,
      dataRetrievalAction, dataRequiredAction, formProvider, view)

  def viewAsString(form: Form[_] = form, officer: Option[String] = None, showVatGroupBullet: Boolean = true) =
    view(form, NormalMode, showVatGroupBullet)(fakeRequest, messages, frontendAppConfig).toString

  "onPageLoad" when {
    "when the VatGroupFlow feature switch is enabled" must {
      "return OK and display the vat group bullet when the VatGroupFlow feature switch is disabled" in {
        disable(VATGroupFlow)
        val result = controller().onPageLoad(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }
    "when the VatGroupFlow feature switch is disabled" must {
      "return OK and not show the vat group bullet when the VatGroupFlow feature switch is enabled" in {
        enable(VATGroupFlow)
        val result = controller().onPageLoad(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(showVatGroupBullet = false)
      }
    }
  }

  "onSubmit" must {
    "redirect to the next page when valid data is submitted" in {
      disable(VATGroupFlow)
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller().onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted with no officer name" in {
      disable(VATGroupFlow)
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit()(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      disable(VATGroupFlow)
      val result = controller(dontGetAnyData).onPageLoad(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      disable(VATGroupFlow)
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller(dontGetAnyData).onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.SessionExpiredController.onPageLoad.url)
    }
  }
}