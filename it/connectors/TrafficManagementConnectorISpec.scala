package connectors

import featureswitch.core.config.FeatureSwitching
import helpers.{AuthHelper, IntegrationSpecBase, SessionStub, TrafficManagementStub}
import models.UKCompany
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

class TrafficManagementConnectorISpec extends IntegrationSpecBase
  with AuthHelper
  with SessionStub
  with TrafficManagementStub
  with FeatureSwitching {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(fakeConfig())
    .build()

  val connector = app.injector.instanceOf[TrafficManagementConnector]

  "POST /traffic-management/:regId/allocate" must {
    "return Allocated when the API responds with CREATED" in {
      stubSuccessfulLogin()
      stubAllocation(testRegId)(CREATED)

      val res = await(connector.allocate(testRegId, UKCompany, isEnrolled = true)(HeaderCarrier()))

      res mustBe Allocated
    }
    "return QuotaReached when the API responds with TOO_MANY_REQUESTS" in {
      stubSuccessfulLogin()
      stubAllocation(testRegId)(TOO_MANY_REQUESTS)

      val res = await(connector.allocate(testRegId, UKCompany, isEnrolled = true)(HeaderCarrier()))

      res mustBe QuotaReached
    }
    "throw an exception for any other status" in {
      stubSuccessfulLogin()
      stubAllocation(testRegId)(IM_A_TEAPOT)

      intercept[Exception] {
        await(connector.allocate(testRegId, UKCompany, isEnrolled = true)(HeaderCarrier()))
      }
    }
  }

}
