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
import forms.ThresholdInTwelveMonthsFormProvider
import identifiers._
import mocks.TrafficManagementServiceMock
import models._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.data.Form
import play.api.libs.json._
import play.api.test.Helpers._
import services.FakeSessionService
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{FakeNavigator, TimeMachine}
import views.html.thresholdInTwelveMonths

import java.time.LocalDate
import scala.concurrent.Future


class ThresholdInTwelveMonthsControllerSpec extends ControllerSpecBase
  with TrafficManagementServiceMock
  with MockSessionService {

  val view = app.injector.instanceOf[thresholdInTwelveMonths]

  def onwardRoute = routes.IndexController.onPageLoad

  object TestTimeMachine extends TimeMachine {
    override def today: LocalDate = LocalDate.parse("2020-01-01")
  }

  val formProvider = new ThresholdInTwelveMonthsFormProvider(timeMachine = TestTimeMachine)
  val form: Form[ConditionalDateFormElement] = formProvider()
  implicit val appConfig: FrontendAppConfig = frontendAppConfig

  val dataRequiredAction = new DataRequiredAction
  val data: Map[String, JsValue] = Map(FixedEstablishmentId.toString -> JsBoolean(true), RegisteringBusinessId.toString -> JsString("own"))
  val getRequiredCacheMap: FakeDataRetrievalAction = getWithCacheMap(CacheMap(cacheMapId, data))

  def controller(dataRetrievalAction: DataRetrievalAction = getRequiredCacheMap) =
    new ThresholdInTwelveMonthsController(controllerComponents, sessionServiceMock, new FakeNavigator(desiredRoute = onwardRoute), FakeCacheIdentifierAction,
      dataRetrievalAction, dataRequiredAction, formProvider, mockTrafficManagementService, view)

  def viewAsString(form: Form[_] = form) = view(form, NormalMode)(fakeDataRequestIncorpedOver12m, messages, frontendAppConfig).toString

  val testInternalId = "id"
  val testRegId = "regId"
  val testDate = LocalDate.now

  "ThresholdInTwelveMonths Controller" must {
    mockSessionRemoveEntry(VoluntaryRegistrationId.toString)(Future.successful(emptyCacheMap))
    mockSessionRemoveEntry(ThresholdNextThirtyDaysId.toString)(Future.successful(emptyCacheMap))

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to start of the journey if user is missing data" in {
      val result = controller(getEmptyCacheMap).onPageLoad(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.IntroductionController.onPageLoad.url)
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val jsValue = Json.toJson(ConditionalDateFormElement(true, Some(LocalDate.now)))
      val validData = data + (ThresholdInTwelveMonthsId.toString -> jsValue)
      val getRelevantData = new FakeDataRetrievalAction(Some(CacheMap(cacheMapId, validData)))

      val result = controller(getRelevantData).onPageLoad(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(ConditionalDateFormElement(true, Some(LocalDate.now))))
    }

    "redirect to the next page when valid data is submitted and also remove Voluntary registration because answer to question is true" in {
      val date = LocalDate.parse("2019-01-01")
      val answer = ConditionalDateFormElement(true, Some(date))
      mockSessionCacheSave[ConditionalDateFormElement](ThresholdInTwelveMonthsId.toString)(answer)(
        Future.successful(emptyCacheMap.copy(data = Map(ThresholdInTwelveMonthsId.toString -> Json.toJson(answer))))
      )
      mockUpsertRegistrationInformation(testInternalId, testRegId, false)(Future.successful(RegistrationInformation(testInternalId, testRegId, Draft, Some(testDate), VatReg)))
      val postRequest = fakeRequest.withFormUrlEncodedBody("value" -> "true",
        "valueDate.year" -> date.getYear.toString,
        "valueDate.month" -> date.getMonthValue.toString
      )

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
