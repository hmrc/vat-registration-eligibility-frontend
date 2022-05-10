package helpers

import config.FrontendAppConfig
import fixtures.BaseA11yFixtures
import models.requests.DataRequest
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.Call
import play.api.test.FakeRequest
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers
import utils.UserAnswers

trait A11ySpec extends AnyWordSpec
  with BaseA11yFixtures
  with Matchers
  with GuiceOneAppPerSuite
  with AccessibilityMatchers {

  implicit val request = FakeRequest()
  val messagesApi = app.injector.instanceOf[MessagesApi]
  implicit val config = app.injector.instanceOf[FrontendAppConfig]
  implicit val fakeDataRequest = DataRequest(FakeRequest("", ""), "foo", "1", new UserAnswers((CacheMap("1", Map()))))
  implicit val messages = messagesApi.preferred(Seq(Lang("en")))

  val testCall: Call = Call("POST", "/test-url")

}
