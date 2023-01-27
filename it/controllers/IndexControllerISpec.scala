package controllers

import helpers.IntegrationSpecBase
import play.api.http.Status.SEE_OTHER
import play.mvc.Http.HeaderNames

import java.net.URLEncoder

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

    "the user is unauthorised" must {
      "redirect to unauthorised page" in new Setup {
        stubUnauthorised()
        stubAudits()

        val res = await(buildClient(s"/journey/$testRegId").get)

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
      }
    }

    "the user is not logged in" must {
      "redirect to sign in url" in new Setup {
        val res = await(buildClientWithoutSession(s"/journey/$testRegId").get)

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(s"${appConfig.loginUrl}?continue=${URLEncoder.encode(appConfig.postSignInUrl, "utf-8")}")
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

    "the user is not authorised" must {
      "redirect to unauthorised page" in new Setup {
        stubUnauthorised()
        stubAudits()

        val res = await(buildClient(s"/question?pageId=mtdInformation&regId=$testRegId").get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
      }
    }

    "the user is not logged in" must {
      "redirect to sign in url" in new Setup {
        val res = await(buildClientWithoutSession(s"/question?pageId=mtdInformation&regId=$testRegId").get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(s"${appConfig.loginUrl}?continue=${URLEncoder.encode(appConfig.postSignInUrl, "utf-8")}")
      }
    }
  }
}