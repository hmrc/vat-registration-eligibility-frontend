package controllers

import helpers.IntegrationSpecBase
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class FeedbackControllerISpec extends IntegrationSpecBase {

  val pageUrl = "/feedback"

  "GET /feedback" must {
    "redirect to the feedback form" in new Setup {
      stubSuccessfulLogin()
      stubAudits()

      val res = await(buildClient(pageUrl).get)
      val referrer = res.headers.get(REFERER).getOrElse("")

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(s"${appConfig.betaFeedbackUrl}&backUrl=$referrer")
    }
  }

}
