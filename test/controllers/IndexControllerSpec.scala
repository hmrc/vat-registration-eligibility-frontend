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
import connectors.mocks.{MockDataCacheConnector, MockS4lConnector}
import controllers.actions.FakeCacheIdentifierAction
import models.CurrentProfile
import models.requests.OptionalDataRequest
import play.api.test.Helpers._
import utils.FakeNavigator

import scala.concurrent.Future

class IndexControllerSpec extends ControllerSpecBase with MockS4lConnector with MockDataCacheConnector{

  implicit val appConfig: FrontendAppConfig = frontendAppConfig

  val testCacheId = "id"
  val testRegId = "regId"
  val request = OptionalDataRequest(fakeRequest, testCacheId, CurrentProfile(testRegId), None)

  class Setup {
    val controller = new IndexController(
      controllerComponents,
      new FakeNavigator(desiredRoute = routes.ThresholdInTwelveMonthsController.onPageLoad),
      dataCacheConnectorMock,
      mockS4LConnector,
      FakeCacheIdentifierAction,
      getEmptyCacheMap
    )
  }

  "onPageLoad" must {
    "Redirect to the Introduction page for a GET" in new Setup {
      mockClearSession(testCacheId)(Future.successful(true))
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
