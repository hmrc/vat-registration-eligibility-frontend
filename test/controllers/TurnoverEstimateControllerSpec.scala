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

import connectors.FakeSessionService
import connectors.mocks.MockSessionService
import controllers.actions._
import forms.TurnoverEstimateFormProvider
import identifiers.{GoneOverThresholdId, TurnoverEstimateId, ZeroRatedSalesId}
import models.{NormalMode, TurnoverEstimateFormElement}
import play.api.data.Form
import play.api.libs.json.{JsBoolean, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{FakeNavigator, Navigator}
import views.html.turnoverEstimate

import scala.concurrent.Future

class TurnoverEstimateControllerSpec extends ControllerSpecBase with MockSessionService {

  def onwardRoute = routes.IndexController.onPageLoad

  val view = app.injector.instanceOf[turnoverEstimate]

  val formProvider = new TurnoverEstimateFormProvider()
  val navigator = app.injector.instanceOf[Navigator]
  val form = formProvider()
  implicit val appConfig = frontendAppConfig

  val dataRequiredAction = new DataRequiredAction

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyCacheMap) =
    new TurnoverEstimateController(controllerComponents, sessionServiceMock, navigator, FakeCacheIdentifierAction,
      dataRetrievalAction, dataRequiredAction, formProvider, view)

  def viewAsString(form: Form[_] = form) = view(form, NormalMode)(fakeDataRequestIncorped, messages, frontendAppConfig).toString

  "TurnoverEstimate Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val element = TurnoverEstimateFormElement("test")
      val validData = Map(TurnoverEstimateId.toString -> Json.toJson(element))
      val getRelevantData = new FakeDataRetrievalAction(Some(CacheMap(cacheMapId, validData)))

      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(element))
    }

    "redirect to the zero rated page when a non-zero value is submitted" in {
      val testTurnover = TurnoverEstimateFormElement("10001")
      mockSessionCacheSave[TurnoverEstimateFormElement](cacheMapId, TurnoverEstimateId.toString)(testTurnover)(Future.successful(CacheMap(
        id = cacheMapId,
        data = Map(TurnoverEstimateId.toString -> Json.toJson(testTurnover))
      )))

      mockSessionCacheSave[Boolean](cacheMapId, ZeroRatedSalesId.toString)(false)(Future.successful(CacheMap(
        id = cacheMapId,
        data = Map(
          ZeroRatedSalesId.toString -> JsBoolean(false),
          TurnoverEstimateId.toString -> Json.toJson(testTurnover)
        )
      )))

      val postRequest = fakeRequest.withFormUrlEncodedBody(("turnoverEstimateAmount", "10001"))
      val result = controller().onSubmit(NormalMode)(postRequest)

      contentAsString(result) mustBe ""
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.ZeroRatedSalesController.onPageLoad.url)
    }

    "skip the zero rated page when zero is submitted" in {
      val testTurnover = TurnoverEstimateFormElement("0")
      mockSessionCacheSave[TurnoverEstimateFormElement](cacheMapId, TurnoverEstimateId.toString)(testTurnover)(Future.successful(CacheMap(
        id = cacheMapId,
        data = Map(TurnoverEstimateId.toString -> Json.toJson(testTurnover))
      )))

      mockSessionCacheSave[Boolean](cacheMapId, ZeroRatedSalesId.toString)(false)(Future.successful(CacheMap(
        id = cacheMapId,
        data = Map(ZeroRatedSalesId.toString -> JsBoolean(false))
      )))

      val postRequest = fakeRequest.withFormUrlEncodedBody(("turnoverEstimateAmount", "0"))
      val result = controller().onSubmit(NormalMode)(postRequest)

      contentAsString(result) mustBe ""
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.VoluntaryInformationController.onPageLoad.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", ""))
      val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.SessionExpiredController.onPageLoad.url)
    }
  }
}
