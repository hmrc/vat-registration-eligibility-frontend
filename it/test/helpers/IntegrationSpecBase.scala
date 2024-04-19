/*
 * Copyright 2024 HM Revenue & Customs
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

package helpers

import org.apache.pekko.util.Timeout
import config.FrontendAppConfig
import featureswitch.core.config.{FeatureSwitching, FeatureSwitchingModule}
import models.CurrentProfile
import models.requests.CacheIdentifierRequest
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.i18n.{Lang, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import services.SessionService
import support.SessionCookieBaker
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.mongo.test.MongoSupport

import scala.concurrent.duration._

trait IntegrationSpecBase extends PlaySpec
  with GuiceOneServerPerSuite
  with ScalaFutures
  with IntegrationPatience
  with SessionCookieBaker
  with WiremockHelper
  with BeforeAndAfterEach
  with BeforeAndAfterAll
  with FutureAwaits
  with DefaultAwaitTimeout
  with MongoSupport
  with FeatureSwitching
  with FakeConfig
  with AuthHelper
  with SessionStub
  with FieldValueChecker {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build()

  val baseUrl = "/check-if-you-can-register-for-vat"
  val mockPort = 11111
  val mockHost = "localhost"
  val url = s"http://$mockHost:$mockPort"
  val defaultUser = "/foo/bar"
  val testRegId = "testRegId"
  val testInternalId = "testInternalId"
  val sessionIdStr = "sessionId"
  val messages = app.injector.instanceOf[MessagesApi].preferred(Seq(Lang("en")))
  implicit val appConfig = app.injector.instanceOf[FrontendAppConfig]
  implicit val request = FakeRequest()
  def request(url: String) = CacheIdentifierRequest(FakeRequest("GET", baseUrl + url), testRegId, testInternalId)

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionIdStr)))

  override def beforeEach(): Unit = {
    super.beforeEach()
    resetWiremock()
    app.injector.instanceOf[FeatureSwitchingModule].switches.foreach(disable)
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    resetAll()
    startWiremock()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    resetAll()
    stopWiremock()
  }

  class Setup(app: Application = app, cacheMap: CacheMap = CacheMap(id = sessionIdStr, data = Map()))(implicit hc: HeaderCarrier) {
    val timeout: Timeout = 5.seconds
    val profileKey = "CurrentProfile"
    val dataKey = "data"

    val sessionService = app.injector.instanceOf[SessionService]

    await(sessionService.save(cacheMap))
    await(sessionService.save(profileKey, CurrentProfile(testRegId)))
  }

}
