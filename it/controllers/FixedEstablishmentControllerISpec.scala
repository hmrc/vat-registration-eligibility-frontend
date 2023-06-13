package controllers

import featureswitch.core.config.FixedEstablishmentExperiment
import helpers.IntegrationSpecBase
import identifiers._
import models.{ConditionalDateFormElement, DateFormElement}
import org.jsoup.Jsoup
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

import java.time.LocalDate

class FixedEstablishmentControllerISpec extends IntegrationSpecBase {

  val pageUrl = "/fixed-establishment"
  val yesRadio = "value"
  val noRadio = "value-no"

  "GET /fixed-establishment" when {

    "FixedEstablishmentExperiment is disabled" when {

      "an answer exists for the page" must {

        "return OK with the answer pre=populated" in new Setup {

          disable(FixedEstablishmentExperiment)

          stubSuccessfulLogin()
          stubAudits()

          cacheSessionData(sessionIdStr, FixedEstablishmentId, true)

          val res = await(buildClient(pageUrl).get)
          val doc = Jsoup.parse(res.body)

          res.status mustBe OK
          doc.radioIsSelected(yesRadio) mustBe true
          doc.radioIsSelected(noRadio) mustBe false
        }
      }

      "an answer doesn't exist for the page" must {
        "return OK with an empty form" in new Setup {

          disable(FixedEstablishmentExperiment)

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

    "FixedEstablishmentExperiment is enabled" when {

      "an answer exists for the page" must {

        "return OK with the answer pre=populated" in new Setup {

          enable(FixedEstablishmentExperiment)

          stubSuccessfulLogin()
          stubAudits()

          cacheSessionData(sessionIdStr, FixedEstablishmentId, true)

          val res = await(buildClient(pageUrl).get)
          val doc = Jsoup.parse(res.body)

          res.status mustBe OK
          doc.radioIsSelected(yesRadio) mustBe true
          doc.radioIsSelected(noRadio) mustBe false
          doc.radioIsSelected("value-uk") mustBe true
          doc.radioIsSelected("value-iom") mustBe false
          doc.radioIsSelected("value-other") mustBe false
        }
      }

      "an answer doesn't exist for the page" must {
        "return OK with an empty form" in new Setup {

          enable(FixedEstablishmentExperiment)

          stubSuccessfulLogin()
          stubAudits()

          val res = await(buildClient(pageUrl).get)
          val doc = Jsoup.parse(res.body)

          res.status mustBe OK
          doc.radioIsSelected(yesRadio) mustBe false
          doc.radioIsSelected(yesRadio) mustBe false
          doc.radioIsSelected(noRadio) mustBe false
          doc.radioIsSelected("value-uk") mustBe false
          doc.radioIsSelected("value-iom") mustBe false
          doc.radioIsSelected("value-other") mustBe false
        }
      }
    }
  }

  "POST /fixed-establishment" when {
    "the user answers 'Yes'" must {
      "redirect to the Business Entity page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map("value" -> "true")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessEntityController.onPageLoad.url)
      }
    }
    "the user answers 'No'" when {
      "redirect to the Business Entity Overseas page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map("value" -> "false")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessEntityOverseasController.onPageLoad.url)
      }
    }

    "the user answers 'Yes' but the question was answered previously" must {
      "redirect to the Business Entity page if the old answer is 'Yes'" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        cacheSessionData(sessionIdStr, FixedEstablishmentId, true)

        val res = await(buildClient(pageUrl).post(Map("value" -> "true")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessEntityController.onPageLoad.url)
      }

      "redirect to the Business Entity page and clear down all threshold data if the old answer is 'No'" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        cacheSessionData(sessionIdStr, FixedEstablishmentId, false)

        cacheSessionData[ConditionalDateFormElement](sessionIdStr, ThresholdInTwelveMonthsId, ConditionalDateFormElement(true, Some(LocalDate.now())))
        cacheSessionData[ConditionalDateFormElement](sessionIdStr, ThresholdNextThirtyDaysId, ConditionalDateFormElement(true, Some(LocalDate.now())))
        cacheSessionData[ConditionalDateFormElement](sessionIdStr, ThresholdNextThirtyDaysId, ConditionalDateFormElement(true, Some(LocalDate.now())))
        cacheSessionData[Boolean](sessionIdStr, VoluntaryRegistrationId, true)
        cacheSessionData[DateFormElement](sessionIdStr, DateOfBusinessTransferId, DateFormElement(LocalDate.now()))
        cacheSessionData[String](sessionIdStr, PreviousBusinessNameId, "test")
        cacheSessionData[String](sessionIdStr, VATNumberId, "test")
        cacheSessionData[Boolean](sessionIdStr, KeepOldVrnId, true)
        cacheSessionData[Boolean](sessionIdStr, TermsAndConditionsId, true)
        cacheSessionData[Boolean](sessionIdStr, TaxableSuppliesInUkId, true)
        cacheSessionData[DateFormElement](sessionIdStr, ThresholdTaxableSuppliesId, DateFormElement(LocalDate.now()))

        val res = await(buildClient(pageUrl).post(Map("value" -> "true")))

        List(
          ThresholdInTwelveMonthsId, ThresholdNextThirtyDaysId, ThresholdNextThirtyDaysId, VoluntaryRegistrationId,
          DateOfBusinessTransferId, PreviousBusinessNameId, VATNumberId, KeepOldVrnId, TermsAndConditionsId,
          TaxableSuppliesInUkId, ThresholdTaxableSuppliesId
        ).foreach(id =>
          verifySessionCacheData[Boolean](sessionIdStr, id, None)
        )

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessEntityController.onPageLoad.url)
      }
    }

    "the user doesn't answer" must {
      "return BAS_REQUEST" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map[String, String]()))

        res.status mustBe BAD_REQUEST
      }
    }
  }

}
