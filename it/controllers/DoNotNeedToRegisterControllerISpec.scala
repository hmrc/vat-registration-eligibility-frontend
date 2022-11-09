package controllers

import helpers.IntegrationSpecBase
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class DoNotNeedToRegisterControllerISpec extends IntegrationSpecBase {

  val pageUrl = "/do-not-need-to-register"

  "GET /do-not-need-to-register" must {
    "return OK" in new Setup {
      stubSuccessfulLogin()
      stubAudits()

      val res = await(buildClient(pageUrl).get)

      res.status mustBe OK
    }
  }

  "POST /do-not-need-to-register" must {
    "log the user out and redirect to the exit survey page" in new Setup {
      stubSuccessfulLogin()
      stubAudits()

      val res = await(buildClient(pageUrl).post(Map[String, String]()))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(appConfig.exitSurveyUrl)
    }
  }

}
