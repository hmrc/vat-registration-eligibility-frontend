package controllers

import helpers.IntegrationSpecBase
import play.api.test.Helpers._

class UnauthorisedControllerISpec extends IntegrationSpecBase {

  val pageUrl = "/unauthorised"

  "GET /unauthorised" must {
    "return OK" in new Setup {
      stubSuccessfulLogin()
      stubAudits()

      val res = await(buildClient(pageUrl).get)

      res.status mustBe OK
    }
  }

}
