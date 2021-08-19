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

import config.FrontendAppConfig
import connectors.FakeDataCacheConnector
import controllers.actions._
import forms.ThresholdPreviousThirtyDaysFormProvider
import identifiers.{ThresholdPreviousThirtyDaysId, VoluntaryRegistrationId}
import models.{ConditionalDateFormElement, NormalMode}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.when
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{FakeNavigator, TimeMachine}
import views.html.thresholdPreviousThirtyDays

import java.time.LocalDate
import scala.concurrent.Future

class ThresholdPreviousThirtyDaysControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = routes.IndexController.onPageLoad

  val view = app.injector.instanceOf[thresholdPreviousThirtyDays]

  object TimeMachine extends TimeMachine {
    override def today: LocalDate = LocalDate.parse("2020-01-01")
  }

  val formProvider = new ThresholdPreviousThirtyDaysFormProvider(timeMachine = TimeMachine)
  val form: Form[ConditionalDateFormElement] = formProvider()
  implicit val appConfig: FrontendAppConfig = frontendAppConfig

  val dataRequiredAction = new DataRequiredAction

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyCacheMap) =
    new ThresholdPreviousThirtyDaysController(controllerComponents, FakeDataCacheConnector, new FakeNavigator(desiredRoute = onwardRoute), FakeCacheIdentifierAction,
      dataRetrievalAction, dataRequiredAction, formProvider, view)

  def viewAsString(form: Form[_] = form) = view(form, NormalMode)(fakeDataRequest, messages, frontendAppConfig).toString


  "ThresholdPreviousThirtyDaysController" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = Map(ThresholdPreviousThirtyDaysId.toString -> Json.toJson(ConditionalDateFormElement(true, Some(LocalDate.now))))
      val getRelevantData = new FakeDataRetrievalAction(Some(CacheMap(cacheMapId, validData)))

      val result = controller(getRelevantData).onPageLoad(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(ConditionalDateFormElement(true, Some(LocalDate.now))))
    }

    "redirect to the next page when valid data is submitted with value of true with a date" in {
      when(mockDataCacheConnector.removeEntry(anyString(), ArgumentMatchers.eq(VoluntaryRegistrationId.toString))) thenReturn Future.successful(emptyCacheMap)
      val date = LocalDate.parse("2019-01-01")
      val postRequest = fakeRequest.withFormUrlEncodedBody("value" -> "true",
        "thresholdPreviousThirtyDaysDate.year" -> date.getYear.toString,
        "thresholdPreviousThirtyDaysDate.month" -> date.getMonthValue.toString,
        "thresholdPreviousThirtyDaysDate.day" -> date.getDayOfMonth.toString
      )

      val result = controller().onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }
    "redirect to the next page when valid data is submitted with value of false" in {
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