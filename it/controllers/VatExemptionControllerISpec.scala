package controllers

import featureswitch.core.config.ExceptionExemptionFlow
import helpers.IntegrationSpecBase
import identifiers.{GoneOverThresholdId, VATExemptionId}
import models.OTRS
import org.jsoup.Jsoup
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class VatExemptionControllerISpec extends IntegrationSpecBase {

  val pageUrl = "/vat-exemption"
  val yesRadio = "value"
  val noRadio = "value-no"

  "GET /vat-exemption" when {
    "an answer exists for the page" must {
      "return OK with the answer pre-populated" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        cacheSessionData(sessionId, VATExemptionId, true)

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

  "POST /vat-exemption" when {
    "the user answers 'Yes'" when {
      "the ExceptionExemptionFlow FS is disabled" must {
        "redirect to the Can't Register (OTRS) page" in new Setup {
          disable(ExceptionExemptionFlow)
          stubSuccessfulLogin()
          stubAudits()

          cacheSessionData(sessionId, GoneOverThresholdId, false)

          val res = await(buildClient(pageUrl).post(Json.obj("value" -> "true")))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.EligibilityDropoutController.onPageLoad(OTRS.toString).url)
        }
      }
      "the ExceptionExemptionFlow FS is enabled" when {
        "the user has gone over the VAT registration threshold" must {
          "redirect to the Mandatory MTD Information page" in new Setup {
            enable(ExceptionExemptionFlow)
            stubSuccessfulLogin()
            stubAudits()

            cacheSessionData(sessionId, GoneOverThresholdId, true)

            val res = await(buildClient(pageUrl).post(Json.obj("value" -> "true")))

            res.status mustBe SEE_OTHER
            res.header(HeaderNames.LOCATION) mustBe Some(routes.MandatoryInformationController.onPageLoad.url)
          }

        }
        "the user hasn't gone over the VAT registration threshold" must {
          "redirect to the Voluntary MTD Information page" in new Setup {
            enable(ExceptionExemptionFlow)
            stubSuccessfulLogin()
            stubAudits()

            cacheSessionData(sessionId, GoneOverThresholdId, false)

            val res = await(buildClient(pageUrl).post(Json.obj("value" -> "true")))

            res.status mustBe SEE_OTHER
            res.header(HeaderNames.LOCATION) mustBe Some(routes.VoluntaryInformationController.onPageLoad.url)
          }
        }
      }
    }
    "the user answers 'No'" when {
      "the user has gone over the VAT registration threshold" must {
        "redirect to the Mandatory MTD Information page" in new Setup {
          disable(ExceptionExemptionFlow)
          stubSuccessfulLogin()
          stubAudits()

          cacheSessionData(sessionId, GoneOverThresholdId, true)

          val res = await(buildClient(pageUrl).post(Json.obj("value" -> "false")))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.MandatoryInformationController.onPageLoad.url)
        }
      }
      "the user hasn't gone over the VAT registration threshold" must {
        "redirect to the Voluntary MTD Information page" in new Setup {
          disable(ExceptionExemptionFlow)
          stubSuccessfulLogin()
          stubAudits()

          cacheSessionData(sessionId, GoneOverThresholdId, false)

          val res = await(buildClient(pageUrl).post(Json.obj("value" -> "false")))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.VoluntaryInformationController.onPageLoad.url)
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
