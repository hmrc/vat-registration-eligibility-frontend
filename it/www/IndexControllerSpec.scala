package www

import helpers.IntegrationSpecBase
import play.mvc.Http.HeaderNames

class IndexControllerSpec extends IntegrationSpecBase {

  s"GET ${controllers.routes.IndexController.navigateToPageId("foo").url}" should {
    "redirect to the start of eligibility because question id is invalid" in {
      val request = buildClient("/question?pageId=foo").get()
      val result = await(request)
      result.status mustBe 303
      result.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdNextThirtyDaysController.onPageLoad().url)
    }
    "redirect to page specified" in {
      val request = buildClient("/question?pageId=annualAccountingScheme").get()
      val result = await(request)
      result.status mustBe 303
      result.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.AnnualAccountingSchemeController.onPageLoad().url)
    }
  }
}