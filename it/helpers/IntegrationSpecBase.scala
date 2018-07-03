package helpers

import akka.util.Timeout
import itutil.WiremockHelper
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.mongo.MongoSpecSupport

import scala.concurrent.duration._

trait IntegrationSpecBase extends PlaySpec
  with OneServerPerSuite
  with ScalaFutures
  with IntegrationPatience
  with WiremockHelper
  with BeforeAndAfterEach
  with BeforeAndAfterAll
  with FutureAwaits
  with DefaultAwaitTimeout
  with MongoSpecSupport with FakeConfig {

  val mockPort = 11111
  val mockHost = "localhost"
  val url = s"http://$mockHost:$mockPort"

  override implicit def defaultAwaitTimeout: Timeout = 5.seconds

  override def beforeEach(): Unit = {
    super.beforeEach()
    resetWiremock()
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    startWiremock()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    stopWiremock()
  }
}
