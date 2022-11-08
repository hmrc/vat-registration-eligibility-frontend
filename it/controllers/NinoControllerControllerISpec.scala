package controllers

import helpers.{IntegrationSpecBase, S4LStub}
import identifiers.NinoId
import org.jsoup.Jsoup
import play.api.http.Status._
import play.api.libs.json.{JsArray, Json}
import play.mvc.Http.HeaderNames

import java.time.LocalDate

class NinoControllerControllerISpec extends IntegrationSpecBase with S4LStub {

  val testDate: LocalDate = LocalDate.now

  val testEnrolments: JsArray = Json.arr(Json.obj(
    "key" -> "IR-CT",
    "identifiers" -> Json.arr(
      Json.obj(
        "key" -> "testKey",
        "value" -> "testValue"
      )
    )
  ))

  val pageUrl = "/have-nino"
  val yesRadio = "value"
  val noRadio = "value-no"

  "GET /have-nino" when {
    "an answer exists for the page" must {
      "return OK with the answer pre-populated" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        cacheSessionData(sessionId, NinoId, true)

        val res = await(buildClient(pageUrl).get)
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.radioIsSelected(yesRadio) mustBe true
        doc.radioIsSelected(noRadio) mustBe false
      }
    }
    "an answer doesn't exist for the page" must {
      "return OK with an empty form" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).get)
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.radioIsSelected(yesRadio) mustBe false
        doc.radioIsSelected(noRadio) mustBe false
      }
    }
  }

  s"POST /have-nino" when {
    "the user answers" must {
      "redirect to VAT Exception if the answer is no" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        val res = await(buildClient("/have-nino").post(Map("value" -> Seq("false"))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VATExceptionKickoutController.onPageLoad.url)
      }
      "redirect to Reg Reason resolver if the answer is yes" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        val res = await(buildClient("/have-nino").post(Map("value" -> Seq("true"))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.RegReasonResolverController.resolve.url)
      }
    }
    "the user doesn't answer" must {
      "return BAD_REQUEST" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient("/have-nino").post(Map[String, String]()))

        res.status mustBe BAD_REQUEST
      }
    }
  }
}