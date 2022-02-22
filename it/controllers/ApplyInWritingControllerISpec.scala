package controllers

import helpers.IntegrationSpecBase
import play.api.test.Helpers._

class ApplyInWritingControllerISpec extends IntegrationSpecBase {

  val pageUrl: String = "/apply-writing"

  "GET /apply-writing" must {
    "return OK" in new Setup {
      stubSuccessfulLogin()
      stubAudits()

      val res = await(buildClient(pageUrl).get)

      res.status mustBe OK
    }
  }

}
