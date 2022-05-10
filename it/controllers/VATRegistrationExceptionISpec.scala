package controllers

import featureswitch.core.config.ExceptionExemptionFlow
import helpers.{IntegrationSpecBase, TrafficManagementStub}
import identifiers.VATRegistrationExceptionId
import models.{Draft, OTRS, RegistrationInformation}
import play.mvc.Http.HeaderNames
import org.jsoup.Jsoup
import play.api.libs.json.Json
import play.api.test.Helpers._

import java.time.LocalDate

class VATRegistrationExceptionISpec extends IntegrationSpecBase with TrafficManagementStub {

  val pageUrl = "/registration-exception"
  val yesRadio = "value"
  val noRadio = "value-no"

  "GET /registration-exception" when {
    "an answer exists for the page" must {
      "return OK with the answer pre-populated" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        cacheSessionData(sessionId, VATRegistrationExceptionId, true)

        val res = await(buildClient(pageUrl).get)
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        doc.radioIsSelected(yesRadio) mustBe true
        doc.radioIsSelected(noRadio) mustBe false
      }
    }
    "an answer doesn't exist for the page" must {
      "return OK with an empty form " in new Setup {
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

  s"POST /registration-exception" when {
    "the user answers" must {
      "ExceptionExemption flow feature switch is not enabled" must {
        s"redirect to ${"/check-if-you-can-register-for-vat/cant-register/vatExceptionKickout"} if answer is yes" in new Setup {
          disable(ExceptionExemptionFlow)
          stubSuccessfulLogin()
          stubUpsertRegistrationInformation(testRegId)(RegistrationInformation(testInternalId, testRegId, Draft, Some(LocalDate.now), OTRS))
          stubAudits()

          val res = await(buildClient("/registration-exception").post(Map("value" -> Seq("true"))))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some("/check-if-you-can-register-for-vat/cant-register/vatExceptionKickout")
        }
        s"redirect to ${controllers.routes.TurnoverEstimateController.onPageLoad} if answer is no" in new Setup {
          stubSuccessfulLogin()
          stubAudits()

          val res = await(buildClient("/registration-exception").post(Map("value" -> Seq("false"))))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TurnoverEstimateController.onPageLoad.url)
        }
      }
      "ExceptionExemption flow feature switch is enabled" must {
        s"redirect to ${controllers.routes.TurnoverEstimateController.onPageLoad} if answer is yes" in new Setup {
          enable(ExceptionExemptionFlow)
          stubSuccessfulLogin()
          stubAudits()

          val res = await(buildClient("/registration-exception").post(Map("value" -> Seq("true"))))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TurnoverEstimateController.onPageLoad.url)
        }
        s"redirect to ${controllers.routes.TurnoverEstimateController.onPageLoad} if answer is no" in new Setup {
          enable(ExceptionExemptionFlow)
          stubSuccessfulLogin()
          stubAudits()

          val res = await(buildClient("/registration-exception").post(Map("value" -> Seq("false"))))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TurnoverEstimateController.onPageLoad.url)
        }
      }
    }
    "the user doesn't answer" must {
      "return BAD_REQUEST" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Json.obj()))

        res.status mustBe BAD_REQUEST
      }
    }
  }
}
