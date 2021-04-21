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
import controllers.actions._
import identifiers.AgriculturalFlatRateSchemeId
import mocks.TrafficManagementServiceMock
import models.{Draft, RegistrationInformation, VatReg}
import play.api.test.Helpers._
import views.html.{agriculturalDropout, internationalActivityDropout, vatDivisionDropout}

import java.time.LocalDate
import scala.concurrent.Future

class EligibilityDropoutControllerSpec extends ControllerSpecBase with TrafficManagementServiceMock {

  def onwardRoute = routes.EligibilityDropoutController.onPageLoad("")

  val internationalDropoutView = app.injector.instanceOf[internationalActivityDropout]
  val agriculturalDropoutView = app.injector.instanceOf[agriculturalDropout]
  val vatDivisionDropoutView = app.injector.instanceOf[vatDivisionDropout]

  implicit val appConfig = app.injector.instanceOf[FrontendAppConfig]

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyCacheMap) =
    new EligibilityDropoutController(
      controllerComponents,
      FakeCacheIdentifierAction,
      fakeDataRetrievalAction,
      new DataRequiredAction,
      mockTrafficManagementService,
      internationalDropoutView,
      agriculturalDropoutView,
      vatDivisionDropoutView)

  val testInternalId = "id"
  val testRegId = "regId"
  val testDate = LocalDate.now

  def internationalView() = internationalDropoutView()(fakeCacheDataRequestIncorped, messages, frontendAppConfig).toString

  def agricultureView() = agriculturalDropoutView()(fakeCacheDataRequestIncorped, messages, appConfig).toString

  "EligibilityDropout Controller" must {

    "return SeeOther and the default view for a GET" in {
      val result = controller().onPageLoad("default")(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(appConfig.otrsUrl)
    }

    "return OK and the International Dropout view for a GET" in {
      val result = controller().internationalActivitiesDropout(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe internationalView()
    }

    "return OK and the Agriculture Dropout view for a GET" in {
      val result = controller().onPageLoad(AgriculturalFlatRateSchemeId.toString)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe agricultureView()
    }

    "redirect to the next page when valid data is submitted" in {
      mockUpsertRegistrationInformation(testInternalId, testRegId, true)(Future.successful(RegistrationInformation(testInternalId, testRegId, Draft, Some(testDate), VatReg)))

      val postRequest = fakeRequest.withFormUrlEncodedBody()

      val result = controller().onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

  }
}




