package controllers

import helpers.IntegrationSpecBase
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class SignOutControllerISpec extends IntegrationSpecBase {

  val pageUrl = "/sign-out"

  "GET /sign-out" must {
    "return OK" in new Setup {
      stubSuccessfulLogin()
      stubAudits()

      val res = await(buildClient(pageUrl).get)

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(appConfig.exitSurveyUrl)
    }
  }

}
