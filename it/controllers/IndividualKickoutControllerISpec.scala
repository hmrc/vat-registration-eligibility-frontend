package controllers

import helpers.IntegrationSpecBase
import play.api.test.Helpers._

class IndividualKickoutControllerISpec extends IntegrationSpecBase {

  val pageUrl: String = routes.IndividualKickoutController.onPageLoad.url

  s"GET $pageUrl" must {
    "return OK" in new Setup {
      stubSuccessfulLogin()
      stubAudits()

      val res = await(buildClient(pageUrl).get)

      res.status mustBe OK
    }
  }

}
