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

import config.FrontendAppConfig
import connectors.mocks.MockSessionService
import controllers.actions._
import forms.ThresholdNextThirtyDaysFormProvider
import identifiers.{ThresholdNextThirtyDaysId, VoluntaryRegistrationId}
import models.{ConditionalDateFormElement, NormalMode}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.when
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeSessionService
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{FakeNavigator, TimeMachine}
import views.html.thresholdNextThirtyDays

import java.time.LocalDate
import scala.concurrent.Future

class ThresholdNextThirtyDaysControllerSpec extends ControllerSpecBase with MockSessionService {

  val view = app.injector.instanceOf[thresholdNextThirtyDays]

  def onwardRoute: Call = routes.IndexController.onPageLoad

  object TestTimeMachine extends TimeMachine {
    override def today: LocalDate = LocalDate.parse("2020-01-01")
  }

  val formProvider = new ThresholdNextThirtyDaysFormProvider(TestTimeMachine)
  val form: Form[ConditionalDateFormElement] = formProvider()
  implicit val appConfig: FrontendAppConfig = frontendAppConfig

  val dataRequiredAction = new DataRequiredAction

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyCacheMap) =
    new ThresholdNextThirtyDaysController(
      controllerComponents,
      sessionServiceMock,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeCacheIdentifierAction,
      dataRetrievalAction,
      dataRequiredAction,
      formProvider,
      view
    )

  def viewAsString(form: Form[_] = form): String = view(form, NormalMode)(fakeDataRequestIncorped, messages, frontendAppConfig).toString

  "ThresholdNextThirtyDays Controller" must {

    mockSessionRemoveEntry(VoluntaryRegistrationId.toString)(Future.successful(emptyCacheMap))

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(fakeDataRequestIncorped)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = Map(ThresholdNextThirtyDaysId.toString -> Json.toJson(ConditionalDateFormElement(value = true, Some(LocalDate.now))))
      val getRelevantData = new FakeDataRetrievalAction(Some(CacheMap(cacheMapId, validData)))

      val result = controller(getRelevantData).onPageLoad(fakeDataRequestIncorped)

      contentAsString(result) mustBe viewAsString(form.fill(ConditionalDateFormElement(value = true, Some(LocalDate.now))))
    }

    "redirect to the next page when valid data is submitted with answer true" in {
      val date = LocalDate.parse("2020-01-01")
      val answer = ConditionalDateFormElement(true, Some(date))
      mockSessionCacheSave[ConditionalDateFormElement](ThresholdNextThirtyDaysId.toString)(answer)(
        Future.successful(emptyCacheMap.copy(data = Map(ThresholdNextThirtyDaysId.toString -> Json.toJson(answer))))
      )
      val postRequest = fakeRequest.withFormUrlEncodedBody("value" -> "true",
        "thresholdNextThirtyDaysDate.year" -> date.getYear.toString,
        "thresholdNextThirtyDaysDate.month" -> date.getMonthValue.toString,
        "thresholdNextThirtyDaysDate.day" -> date.getDayOfMonth.toString
      )

      val result = controller().onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to the next page when valid data is submitted with answer false" in {
      val answer = ConditionalDateFormElement(false, None)
      mockSessionCacheSave[ConditionalDateFormElement](ThresholdNextThirtyDaysId.toString)(answer)(
        Future.successful(emptyCacheMap.copy(data = Map(ThresholdNextThirtyDaysId.toString -> Json.toJson(answer))))
      )
      val postRequest = fakeRequest.withFormUrlEncodedBody("value" -> "false")

      val result = controller().onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit()(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller(dontGetAnyData).onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.SessionExpiredController.onPageLoad.url)
    }
  }
}
