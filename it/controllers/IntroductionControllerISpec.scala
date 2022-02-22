package controllers

import helpers.IntegrationSpecBase
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class IntroductionControllerISpec extends IntegrationSpecBase {

  val testUrl = controllers.routes.IntroductionController.onPageLoad.url

  "GET /introduction" must {
    "return OK" in new Setup {
      stubSuccessfulLogin()
      stubAudits()

      val res = await(buildClient(testUrl).get)

      res.status mustBe OK
    }
  }

  "POST /introduction" must {
    "Redirect to the FixedEstablishmentController" in new Setup {
      stubSuccessfulLogin()
      stubAudits()

      val res = await(buildClient(testUrl).post(Json.obj()))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.FixedEstablishmentController.onPageLoad.url)
    }
  }

}
