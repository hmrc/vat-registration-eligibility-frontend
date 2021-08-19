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

import connectors.{Allocated, FakeDataCacheConnector, QuotaReached}
import controllers.actions._
import featureswitch.core.config.{FeatureSwitching, TrafficManagement}
import forms.NinoFormProvider
import identifiers.{BusinessEntityId, NinoId}
import mocks.{S4LServiceMock, TrafficManagementServiceMock}
import models._
import models.requests.DataRequest
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.data.Form
import play.api.libs.json.{JsBoolean, JsValue, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{FakeIdGenerator, FakeNavigator, FakeTimeMachine, UserAnswers}
import views.html.nino

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class NinoControllerSpec extends ControllerSpecBase with FeatureSwitching with TrafficManagementServiceMock with S4LServiceMock {

  def onwardRoute: Call = routes.ThresholdInTwelveMonthsController.onPageLoad

  val view = app.injector.instanceOf[nino]

  val formProvider = new NinoFormProvider()
  val form = formProvider()
  implicit val appConfig = frontendAppConfig

  val dataRequiredAction = new DataRequiredAction

  val timeMachine = new FakeTimeMachine
  val idGenerator = new FakeIdGenerator

  val testInternalId = "id"
  val testRegId = "regId"
  val testProviderId: String = "testProviderID"
  val testProviderType: String = "GovernmentGateway"
  val testCredentials: Credentials = Credentials(testProviderId, testProviderType)
  val testCacheMap = CacheMap(testRegId, Map(NinoId.toString -> JsBoolean(true)))
  val testRegistrationInformation = RegistrationInformation(testInternalId, testRegId, Draft, regStartDate = Some(testDate), VatReg)
  val testDate = LocalDate.now

  def testPostRequest(postData: (String, String)*) =
    DataRequest(fakeRequest.withFormUrlEncodedBody(postData:_*), testInternalId, CurrentProfile(testRegId), new UserAnswers(CacheMap(testRegId, Map())))

  val data: Map[String, JsValue] = Map(BusinessEntityId.toString -> Json.toJson(UKCompany))
  val getRequiredCacheMap: FakeDataRetrievalAction = getWithCacheMap(CacheMap(cacheMapId, data))

  def controller(dataRetrievalAction: DataRetrievalAction = getRequiredCacheMap) =
    new NinoController(controllerComponents, FakeDataCacheConnector, mockS4LService, new FakeNavigator(desiredRoute = onwardRoute), FakeCacheIdentifierAction,
      dataRetrievalAction, dataRequiredAction, formProvider, mockTrafficManagementService, view)

  def viewAsString(form: Form[_] = form) = view(form, NormalMode)(fakeDataRequest, messages, frontendAppConfig).toString

  "Nino Controller" must {
    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = Map(NinoId.toString -> JsBoolean(true))
      val getRelevantData = new FakeDataRetrievalAction(Some(CacheMap(cacheMapId, validData)))

      val result = controller(getRelevantData).onPageLoad(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(true))
    }

    "redirect to the next page when valid data is submitted and Traffic Management returns Allocated" in {
      enable(TrafficManagement)
      mockServiceAllocation(testRegId, UKCompany)(Future.successful(Allocated))
      mockGetRegistrationInformation()(Future.successful(Some(RegistrationInformation(testInternalId, testRegId, Draft, Some(testDate), VatReg))))
      mockS4LSave(testRegId, "eligibility-data", Json.toJson(testCacheMap))(Future.successful(testCacheMap))
      when(
        mockAuthConnector.authorise(
          ArgumentMatchers.any,
          ArgumentMatchers.eq(Retrievals.credentials)
        )(
          ArgumentMatchers.any[HeaderCarrier],
          ArgumentMatchers.any[ExecutionContext])
      ).thenReturn(Future.successful(Some(testCredentials)))

      val result = controller().onSubmit()(testPostRequest("value" -> "true"))

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to the exception page when no is selected" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "false"))

      val result = controller().onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.VATExceptionKickoutController.onPageLoad.url)
    }

    "redirect to the next page when the Traffic Management is disabled" in {
      disable(TrafficManagement)

      mockUpsertRegistrationInformation(testInternalId, testRegId, isOtrs = false)(Future.successful(testRegistrationInformation))
      val postRequest = testPostRequest("value" -> "true")

      val result = controller().onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to the exception page when valid data is submitted, Traffic Management returns Quota Reached and RegistrationInformation does not match" in {
      enable(TrafficManagement)
      mockServiceAllocation(testRegId, UKCompany)(Future.successful(QuotaReached))
      mockGetRegistrationInformation()(Future.successful(Some(RegistrationInformation(testInternalId, testRegId, Draft, Some(testDate), OTRS))))
      when(
        mockAuthConnector.authorise(
          ArgumentMatchers.any,
          ArgumentMatchers.eq(Retrievals.credentials)
        )(
          ArgumentMatchers.any[HeaderCarrier],
          ArgumentMatchers.any[ExecutionContext])
      ).thenReturn(Future.successful(Some(testCredentials)))

      val result = controller().onSubmit()(testPostRequest("value" -> "true"))

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.VATExceptionKickoutController.onPageLoad.url)
    }

    "redirect to the next page when valid data is submitted, Traffic Management returns Quota Reached but RegistrationInformation does match" in {
      enable(TrafficManagement)

      mockServiceAllocation(testRegId, UKCompany)(Future.successful(QuotaReached))
      mockGetRegistrationInformation()(Future.successful(Some(RegistrationInformation(testInternalId, testRegId, Draft, Some(testDate), VatReg))))

      when(
        mockAuthConnector.authorise(
          ArgumentMatchers.any,
          ArgumentMatchers.eq(Retrievals.credentials)
        )(
          ArgumentMatchers.any[HeaderCarrier],
          ArgumentMatchers.any[ExecutionContext])
      ).thenReturn(Future.successful(Some(testCredentials)))

      val result = controller().onSubmit()(testPostRequest("value" -> "true"))

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
