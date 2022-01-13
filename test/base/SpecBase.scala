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

package base

import config.FrontendAppConfig
import controllers.actions.FakeDataRetrievalAction
import models.CurrentProfile
import models.requests.{CacheIdentifierRequest, DataRequest}
import org.scalatestplus.play.guice._
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.UserAnswers

import scala.concurrent.ExecutionContext

trait SpecBase extends CommonSpecBase with GuiceOneAppPerSuite {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure("metrics.enabled" -> "false")
    .build()

  def injector: Injector = app.injector

  implicit def frontendAppConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  def controllerComponents: MessagesControllerComponents = injector.instanceOf[MessagesControllerComponents]

  implicit def executionContext: ExecutionContext = injector.instanceOf[ExecutionContext]

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  def fakeRequest = FakeRequest("", "")

  def fakeDataRetrievalAction = new FakeDataRetrievalAction(Some(CacheMap("regId", Map())))

  def fakeDataRequestIncorped = new DataRequest(fakeRequest, "foo", "1", new UserAnswers((CacheMap("1", Map()))))

  def fakeDataRequestIncorpedOver12m = new DataRequest(fakeRequest, "foo", "1", new UserAnswers((CacheMap("1", Map()))))

  def fakeDataRequest = new DataRequest(fakeRequest, "foo", "1", new UserAnswers(CacheMap("1", Map())))

  def fakeCacheDataRequestIncorped = new CacheIdentifierRequest(fakeRequest, "foo", "1")

  def messages: Messages = messagesApi.preferred(fakeRequest)

  def messagesIncorped: Messages = messagesApi.preferred(fakeDataRequestIncorped)
}
