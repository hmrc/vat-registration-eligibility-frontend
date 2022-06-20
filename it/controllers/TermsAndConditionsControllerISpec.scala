package controllers

import helpers.{IntegrationSpecBase, S4LStub}
import play.api.http.Status._
import play.mvc.Http.HeaderNames

class TermsAndConditionsControllerISpec extends IntegrationSpecBase with S4LStub {

  val pageUrl = "/terms-and-conditions"

  s"GET /terms-and-conditions" must {
    "return OK" in new Setup {
      stubSuccessfulLogin()
      stubAudits()
      stubS4LGetNothing(testRegId)

      val res = await(buildClient(pageUrl).get)

      res.status mustBe OK
    }
  }

  s"POST /terms-and-conditions" when {
    "the answer is 'Yes'" must {
      "redirect to Mtd information page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        val res = await(buildClient(pageUrl).post(Map("termsAndConditions" -> Seq("true"))))

        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.MtdInformationController.onPageLoad.url)
      }
    }
    "the answer is 'No'" must {
      "redirect to Mtd information page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        val res = await(buildClient(pageUrl).post(Map("termsAndConditions" -> Seq("false"))))

        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.MtdInformationController.onPageLoad.url)
      }
    }
  }
}