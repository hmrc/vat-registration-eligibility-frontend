package controllers

import helpers.IntegrationSpecBase
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class MandatoryInformationControllerISpec extends IntegrationSpecBase {

  val pageUrl = "/mtd-mandatory-information"

  "GET /mtd-mandatory-information" must {
    "return OK" in new Setup {
      stubSuccessfulLogin()
      stubAudits()

      val res = await(buildClient(pageUrl).get)

      res.status mustBe OK
    }
  }

  "POST /mtd-mandatory-information" must {
    "redirect to Eligible controller" in new Setup {
      stubSuccessfulLogin()
      stubAudits()

      val res = await(buildClient(pageUrl).post(Json.obj()))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.EligibleController.onPageLoad.url)
    }
  }

}
