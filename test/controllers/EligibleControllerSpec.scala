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

import controllers.actions.{DataRequiredAction, FakeCacheIdentifierAction}
import mocks.TrafficManagementServiceMock
import models.requests.DataRequest
import models.{Draft, RegistrationInformation, VatReg}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.eligible

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class EligibleControllerSpec extends ControllerSpecBase with TrafficManagementServiceMock {

  implicit val appConfig = frontendAppConfig

  val view = app.injector.instanceOf[eligible]

  val dataRequiredAction = new DataRequiredAction

  object Controller extends EligibleController(
    controllerComponents,
    identify = FakeCacheIdentifierAction,
    getData = fakeDataRetrievalAction,
    requireData = dataRequiredAction,
    vatRegistrationService = mockVRService,
    mockTrafficManagementService,
    view
  )

  def viewAsString = view()(fakeRequest, messages, frontendAppConfig).toString

  val testInternalId = "id"
  val testRegId = "regId"
  val testDate = LocalDate.now

  "onPageLoad" must {
    "return OK with the correct view" in {
      val res = Controller.onPageLoad()(fakeRequest)
      status(res) mustBe OK
      contentAsString(res) mustBe viewAsString
    }
  }

  "onSubmit" must {
    "redirect to VAT reg frontend" in {
      when(mockVRService.submitEligibility(ArgumentMatchers.any[String])(ArgumentMatchers.any[HeaderCarrier], ArgumentMatchers.any[ExecutionContext], ArgumentMatchers.any[DataRequest[_]]))
        .thenReturn(Future.successful(Json.obj()))

      mockUpsertRegistrationInformation(testInternalId, testRegId, false)(
        Future.successful(RegistrationInformation(testInternalId, testRegId, Draft, Some(testDate), VatReg))
      )

      val res = Controller.onSubmit()(fakeRequest)

      status(res) mustBe SEE_OTHER
      redirectLocation(res) must contain("http://localhost:9895/register-for-vat/honesty-declaration")
    }
  }

}
