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
import forms.BusinessEntityPartnershipFormProvider
import identifiers.BusinessEntityId
import models.BusinessEntity.{limitedPartnershipKey, ukCompanyKey}
import models._
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.FakeNavigator
import views.html.businessEntityPartnership

class BusinessEntityPartnershipControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = routes.IndexController.onPageLoad()

  val view: businessEntityPartnership = app.injector.instanceOf[businessEntityPartnership]
  val formProvider = new BusinessEntityPartnershipFormProvider()
  val form: Form[PartnershipType] = formProvider()
  implicit val appConfig: FrontendAppConfig = frontendAppConfig
  val testCall: Call = Call("POST", "/check-if-you-can-register-for-vat/business-entity-partnership")
  val dataRequiredAction = new DataRequiredAction
  val postAction: Call = testCall

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyCacheMap) = new BusinessEntityPartnershipController(
    controllerComponents,
    FakeDataCacheConnector,
    new FakeNavigator(desiredRoute = onwardRoute),
    FakeCacheIdentifierAction,
    dataRetrievalAction,
    dataRequiredAction,
    formProvider,
    view
  )

  def viewAsString(form: Form[PartnershipType] = form): String = view(form, postAction)(fakeDataRequestIncorped, messages, frontendAppConfig).toString

  "RegisteringBusiness Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad()(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = Map(BusinessEntityId.toString -> Json.toJson(LimitedPartnership))
      val getRelevantData = new FakeDataRetrievalAction(Some(CacheMap(cacheMapId, validData)))

      val result = controller(getRelevantData).onPageLoad()(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(LimitedPartnership))
    }

    "populate the view with a partnership on a GET when the user is a scottish partnership" in {
      val validData = Map(BusinessEntityId.toString -> Json.toJson(Partnership))
      val getRelevantData = new FakeDataRetrievalAction(Some(CacheMap(cacheMapId, validData)))

      val result = controller(getRelevantData).onPageLoad()(fakeRequest)

      contentAsString(result) mustBe viewAsString(form)
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", limitedPartnershipKey))

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
      val result = controller(dontGetAnyData).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", Json.toJson(UKCompany).toString))
      val result = controller(dontGetAnyData).onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
