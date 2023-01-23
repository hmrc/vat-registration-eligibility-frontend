package controllers

import helpers.IntegrationSpecBase
import play.api.http.Status.SEE_OTHER
import play.mvc.Http.HeaderNames

class IndexControllerISpec extends IntegrationSpecBase {

  "GET /" must {
    "redirect to the Introduction page" in new Setup {
      stubSuccessfulLogin()
      stubAudits()

      val res = await(buildClient("/").get)

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.FixedEstablishmentController.onPageLoad.url)
    }
  }

  "GET /journey/:regId" when {
    "the user is authorised" must {
      "redirect to the introduction page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(s"/journey/$testRegId").get)

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.FixedEstablishmentController.onPageLoad.url)
      }
    }
  }

  s"GET ${controllers.routes.IndexController.navigateToPageId("foo", testRegId).url}" must {
    "the user is authorised" must {
      "redirect to the start of eligibility because question id is invalid" in {
        stubSuccessfulLogin()
        stubAudits()

        val result = await(buildClient(s"/question?pageId=foo&regId=$testRegId").get())

        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.FixedEstablishmentController.onPageLoad.url)
      }
      "redirect to page specified" in {
        stubSuccessfulLogin()
        stubAudits()

        val result = await(buildClient(s"/question?pageId=mtdInformation&regId=$testRegId").get())

        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.MtdInformationController.onPageLoad.url)
      }
    }
  }
}