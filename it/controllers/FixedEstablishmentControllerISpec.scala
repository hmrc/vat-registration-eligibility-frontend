package controllers

import featureswitch.core.config.NonUkCompanyFlow
import helpers.IntegrationSpecBase
import identifiers._
import models.{ConditionalDateFormElement, DateFormElement}
import org.jsoup.Jsoup
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

import java.time.LocalDate

class FixedEstablishmentControllerISpec extends IntegrationSpecBase {

  val pageUrl = "/fixed-establishment"
  val yesRadio = "value"
  val noRadio = "value-no"

  "GET /fixed-establishment" when {
    "an answer exists for the page" must {
      "return OK with the answer pre=populated" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        cacheSessionData(sessionId, FixedEstablishmentId, true)

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

  "POST /fixed-establishment" when {
    "the user answers 'Yes'" must {
      "redirect to the Business Entity page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Json.obj("value" -> "true")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessEntityController.onPageLoad.url)
      }
    }
    "the user answers 'No'" when {
      "redirect to the Business Entity Overseas page" in new Setup {
        enable(NonUkCompanyFlow)
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Json.obj("value" -> "false")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessEntityOverseasController.onPageLoad.url)
      }
    }

    "the user answers 'Yes' but the question was answered previously" must {
      "redirect to the Business Entity page if the old answer is 'Yes'" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        cacheSessionData(sessionId, FixedEstablishmentId, true)

        val res = await(buildClient(pageUrl).post(Json.obj("value" -> "true")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessEntityController.onPageLoad.url)
      }

      "redirect to the Business Entity page and clear down all threshold data if the old answer is 'No'" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        cacheSessionData(sessionId, FixedEstablishmentId, false)

        cacheSessionData[ConditionalDateFormElement](sessionId, ThresholdInTwelveMonthsId, ConditionalDateFormElement(true, Some(LocalDate.now())))
        cacheSessionData[ConditionalDateFormElement](sessionId, ThresholdNextThirtyDaysId, ConditionalDateFormElement(true, Some(LocalDate.now())))
        cacheSessionData[ConditionalDateFormElement](sessionId, ThresholdNextThirtyDaysId, ConditionalDateFormElement(true, Some(LocalDate.now())))
        cacheSessionData[Boolean](sessionId, VoluntaryRegistrationId, true)
        cacheSessionData[DateFormElement](sessionId, DateOfBusinessTransferId, DateFormElement(LocalDate.now()))
        cacheSessionData[String](sessionId, PreviousBusinessNameId, "test")
        cacheSessionData[String](sessionId, VATNumberId, "test")
        cacheSessionData[Boolean](sessionId, KeepOldVrnId, true)
        cacheSessionData[Boolean](sessionId, TermsAndConditionsId, true)
        cacheSessionData[Boolean](sessionId, TaxableSuppliesInUkId, true)
        cacheSessionData[DateFormElement](sessionId, ThresholdTaxableSuppliesId, DateFormElement(LocalDate.now()))

        val res = await(buildClient(pageUrl).post(Json.obj("value" -> "true")))

        List(
          ThresholdInTwelveMonthsId, ThresholdNextThirtyDaysId, ThresholdNextThirtyDaysId, VoluntaryRegistrationId,
          DateOfBusinessTransferId, PreviousBusinessNameId, VATNumberId, KeepOldVrnId, TermsAndConditionsId,
          TaxableSuppliesInUkId, ThresholdTaxableSuppliesId
        ).foreach(id =>
          verifySessionCacheData[Boolean](sessionId, id, None)
        )

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessEntityController.onPageLoad.url)
      }
    }

    "the user doesn't answer" must {
      "return BAS_REQUEST" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Json.obj()))

        res.status mustBe BAD_REQUEST
      }
    }
  }

}
