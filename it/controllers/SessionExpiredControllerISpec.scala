package controllers

import helpers.IntegrationSpecBase
import play.api.test.Helpers._

class SessionExpiredControllerISpec extends IntegrationSpecBase {

  val pageUrl = "/this-service-has-been-reset"

  "GET /this-service-has-been-reset" must {
    "return OK" in new Setup {
      stubSuccessfulLogin()
      stubAudits()

      val res = await(buildClient(pageUrl).get)

      res.status mustBe OK
    }
  }

}
