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
import connectors.mocks.{MockS4lConnector, MockSessionService}
import controllers.actions.FakeCacheIdentifierAction
import models.CurrentProfile
import models.requests.OptionalDataRequest
import org.mockito.ArgumentMatchers
import play.api.test.Helpers._
import utils.FakeNavigator

import scala.concurrent.{ExecutionContext, Future}
import org.mockito.Mockito._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.HeaderCarrier

class IndexControllerSpec extends ControllerSpecBase with MockS4lConnector with MockSessionService{

  implicit val appConfig: FrontendAppConfig = frontendAppConfig

  val testInternalId = "id"
  val testRegId = "regId"
  val request = OptionalDataRequest(fakeRequest, testRegId, testInternalId, None)

  class Setup {
    val controller = new IndexController(
      mockAuthConnector,
      controllerComponents,
      new FakeNavigator(desiredRoute = routes.ThresholdInTwelveMonthsController.onPageLoad),
      FakeCacheIdentifierAction,
      getEmptyCacheMap,
      mockJourneyService
    )
  }

  "initialiseJourney" when {
    "the user is authorised and the journey has been set up" must {
      "redirect to the Introduction page" in new Setup {
        when(mockAuthConnector.authorise(ArgumentMatchers.any, ArgumentMatchers.eq(Retrievals.internalId))(
          ArgumentMatchers.any[HeaderCarrier],
          ArgumentMatchers.any[ExecutionContext]
        )).thenReturn(Future.successful(Some(testInternalId)))

        when(mockJourneyService.initialiseJourney(
          ArgumentMatchers.eq(testInternalId),
          ArgumentMatchers.eq(testRegId)
        )(ArgumentMatchers.any[HeaderCarrier]))
          .thenReturn(Future.successful(emptyCacheMap))

        val res = controller.initJourney(testRegId)(request)

        status(res) mustBe SEE_OTHER
        redirectLocation(res) mustBe Some(routes.IntroductionController.onPageLoad.url)
      }
    }
  }

  "onPageLoad" must {
    "Redirect to the Introduction page for a GET" in new Setup {
      mockClearSession(Future.successful(true))
      mockS4LClear()
      val result = controller.onPageLoad(request)
      redirectLocation(result) mustBe Some(routes.IntroductionController.onPageLoad.url)
    }
  }
  "navigateToPage with a page id takes user to page in navigator" in new Setup {
    val result = controller.navigateToPageId("foo")(fakeRequest)
    redirectLocation(result) mustBe Some(routes.IntroductionController.onPageLoad.url)
  }
}
