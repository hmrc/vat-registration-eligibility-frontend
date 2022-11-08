package controllers

import helpers.IntegrationSpecBase
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class ChooseNotToRegisterControllerISpec extends IntegrationSpecBase {

  val pageUrl = "/chosen-not-to-register"

  "GET /chosen-not-to-register" must {
    "return OK" in new Setup {
      stubSuccessfulLogin()
      stubAudits()

      val res = await(buildClient(pageUrl).get)

      res.status mustBe OK
    }
  }

  "POST /chosen-not-to-register" must {
    "log the user out and redirect to the exit survey page" in new Setup {
      stubSuccessfulLogin()
      stubAudits()

      val res = await(buildClient(pageUrl).post(Map[String, String]()))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(appConfig.exitSurveyUrl)
    }
  }

}

