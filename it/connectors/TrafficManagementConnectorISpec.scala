package connectors

import featureswitch.core.config.FeatureSwitching
import helpers.{IntegrationSpecBase, TrafficManagementStub}
import models.UKCompany
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

class TrafficManagementConnectorISpec extends IntegrationSpecBase
  with TrafficManagementStub
  with FeatureSwitching {

  val connector = app.injector.instanceOf[TrafficManagementConnector]

  "POST /traffic-management/:regId/allocate" must {
    "return Allocated when the API responds with CREATED" in {
      stubSuccessfulLogin()
      stubAllocation(testRegId)(CREATED)
      stubAudits()

      val res = await(connector.allocate(testRegId, UKCompany, isEnrolled = true)(HeaderCarrier()))

      res mustBe Allocated
    }
    "return QuotaReached when the API responds with TOO_MANY_REQUESTS" in {
      stubSuccessfulLogin()
      stubAllocation(testRegId)(TOO_MANY_REQUESTS)
      stubAudits()

      val res = await(connector.allocate(testRegId, UKCompany, isEnrolled = true)(HeaderCarrier()))

      res mustBe QuotaReached
    }
    "throw an exception for any other status" in {
      stubSuccessfulLogin()
      stubAllocation(testRegId)(IM_A_TEAPOT)
      stubAudits()

      intercept[Exception] {
        await(connector.allocate(testRegId, UKCompany, isEnrolled = true)(HeaderCarrier()))
      }
    }
  }

}
