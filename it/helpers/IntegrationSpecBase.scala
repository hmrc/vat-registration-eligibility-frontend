package helpers

import akka.util.Timeout
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


  override implicit def defaultAwaitTimeout: Timeout = 5.seconds
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

    await(sessionService.save(cacheMap))(timeout)
    await(sessionService.save(profileKey, CurrentProfile(testRegId)))(timeout)
  }

}
