package controllers

import controllers.actions._
import play.api.test.Helpers._
import views.html.$className;format="decap"$

class $className$ControllerSpec extends ControllerSpecBase {

  def onwardRoute = routes.$className;format="cap"$Controller.onPageLoad()

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyCacheMap) =
    new $className$Controller(frontendAppConfig, messagesApi, FakeCacheIdentifierAction)

  def viewAsString() = $className;format="decap"$(frontendAppConfig)(fakeRequest, messages).toString

  "$className$ Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody()

      val result = controller().onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

  }
}




