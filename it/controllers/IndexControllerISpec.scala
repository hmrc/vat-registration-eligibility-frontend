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

  s"GET ${controllers.routes.IndexController.navigateToPageId("foo").url}" should {
    "redirect to the start of eligibility because question id is invalid" in {
      val result = await(buildClient("/question?pageId=foo").get())

      result.status mustBe SEE_OTHER
      result.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.FixedEstablishmentController.onPageLoad.url)
    }
    "redirect to page specified" in {
      val result = await(buildClient("/question?pageId=zeroRatedSales").get())

      result.status mustBe SEE_OTHER
      result.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ZeroRatedSalesController.onPageLoad.url)
    }
  }
}