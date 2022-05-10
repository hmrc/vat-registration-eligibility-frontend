package controllers

import featureswitch.core.config.FeatureSwitching
import helpers.{IntegrationSpecBase, S4LStub}
import identifiers.VATNumberId
import org.jsoup.Jsoup
import play.api.http.Status.OK
import play.mvc.Http.HeaderNames

class VATNumberControllerISpec extends IntegrationSpecBase with FeatureSwitching with S4LStub {

  val pageUrl = "/vat-number"
  val testVatNumber = "123456782"
  val textbox = "vatNumber"

  s"GET /vat-number" must {
    "render the page" when {
      "an answer doesn't exist for the page" must {
        "return OK wth an empty form" in new Setup {
          stubSuccessfulLogin()
          stubAudits()
          val nothing = ""

          val res = await(buildClient(pageUrl).get)
          val doc = Jsoup.parse(res.body)

          res.status mustBe OK
          doc.textboxContainsValue(textbox, nothing) mustBe true
        }
      }
      "an answer exists for the page" must {
        "return OK with the answer pre-populated" in new Setup {
          stubSuccessfulLogin()
          stubAudits()

          cacheSessionData[String](sessionId, VATNumberId, testVatNumber)

          val res = await(buildClient(pageUrl).get)
          val doc = Jsoup.parse(res.body)

          res.status mustBe OK
          doc.textboxContainsValue(textbox, testVatNumber) mustBe true
        }
      }
    }

    s"POST /vat-number" must {
      "redirect to Keep existing VRN" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        val request = buildClient(pageUrl).post(Map("vatNumber" -> testVatNumber))

        val response = await(request)
        response.status mustBe 303
        response.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.KeepOldVrnController.onPageLoad.url)
      }

      "return a BAD_REQUEST with form errors" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        val request = buildClient(pageUrl).post(Map("vatNumber" -> ""))

        val response = await(request)
        response.status mustBe 400
      }
    }
  }
}
