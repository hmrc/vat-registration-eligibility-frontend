package controllers

import helpers.{IntegrationSpecBase, S4LStub}
import identifiers._
import models.RegistrationReason._
import models._
import org.jsoup.Jsoup
import play.api.http.Status._
import play.mvc.Http.HeaderNames

import java.time.LocalDate

class RegistrationReasonControllerISpec extends IntegrationSpecBase with S4LStub {

  val pageUrl = "/registration-reason"

  val options = Map(
    SellingGoodsAndServices -> sellingGoodsAndServicesKey,
    TakingOverBusiness -> takingOverBusinessKey,
    ChangingLegalEntityOfBusiness -> changingLegalEntityOfBusinessKey,
    UkEstablishedOverseasExporter -> ukEstablishedOverseasExporterKey
  )

  "GET /registration-reason" when {
    "an answer exists for the page" when {
      options.foreach { case (answer, radioKey) =>
        s"when the option is ${answer.toString()}" must {
          "return OK with the answer pre-populated" in new Setup {
            stubSuccessfulLogin()
            stubAudits()
            stubS4LGetNothing(testRegId)

            cacheSessionData[RegistrationReason](sessionIdStr, RegistrationReasonId, answer)

            val res = await(buildClient(pageUrl).get)
            val doc = Jsoup.parse(res.body)

            res.status mustBe OK
            doc.radioIsSelected(radioKey) mustBe true

            options.filterNot(_._1 == answer).foreach { case (_, unselectedOption) =>
              doc.radioIsSelected(unselectedOption) mustBe false
            }
          }
        }
      }
      "when the answer is SettingUpVatGroup" must {
        "return OK with the answer pre-populated" in new Setup {
          stubSuccessfulLogin()
          stubAudits()
          stubS4LGetNothing(testRegId)

          cacheSessionData[BusinessEntity](sessionIdStr, BusinessEntityId, UKCompany)
          cacheSessionData[RegistrationReason](sessionIdStr, RegistrationReasonId, SettingUpVatGroup)

          val res = await(buildClient(pageUrl).get)
          val doc = Jsoup.parse(res.body)

          res.status mustBe OK
          doc.radioIsSelected(settingUpVatGroupKey) mustBe true
        }
      }
      "when an answer doesn't exist for the page" must {
        "return OK with an empty form" in new Setup {
          stubSuccessfulLogin()
          stubAudits()
          stubS4LGetNothing(testRegId)

          val res = await(buildClient(pageUrl).get)
          val doc = Jsoup.parse(res.body)

          res.status mustBe OK

          options.foreach { case (_, unselectedOption) =>
            doc.radioIsSelected(unselectedOption) mustBe false
          }
        }
      }
    }
  }

  "POST /registration-reason" when {
    "the user answer" when {
      s"redirect to Reg Reason Resolver when sellingGoodsAndServices value is selected" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        cacheSessionData[Boolean](sessionIdStr, FixedEstablishmentId, true)

        val res = await(buildClient(pageUrl).post(Map("value" -> sellingGoodsAndServicesKey)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.RegReasonResolverController.resolve.url)
      }

      s"redirect to Reg Reason Resolver when ukEstablishedOverseasExporter value is selected" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        cacheSessionData[Boolean](sessionIdStr, FixedEstablishmentId, true)

        val res = await(buildClient(pageUrl).post(Map("value" -> ukEstablishedOverseasExporterKey)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.RegReasonResolverController.resolve.url)
      }

      s"redirect to Reg Reason Resolver when settingUpVatGroup value is selected" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        cacheSessionData[Boolean](sessionIdStr, FixedEstablishmentId, true)

        val res = await(buildClient(pageUrl).post(Map("value" -> settingUpVatGroupKey)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.RegReasonResolverController.resolve.url)
      }

      s"redirect to Reg Reason Resolver for an overseas user selecting takingOverBusiness" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        cacheSessionData[Boolean](sessionIdStr, FixedEstablishmentId, false)
        cacheSessionData[BusinessEntity](sessionIdStr, BusinessEntityId, Overseas)

        val res = await(buildClient(pageUrl).post(Map("value" -> takingOverBusinessKey)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.RegReasonResolverController.resolve.url)
      }

      s"redirect to Reg Reason Resolver for an overseas user selecting changingLegalEntityOfBusiness" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        cacheSessionData[Boolean](sessionIdStr, FixedEstablishmentId, false)
        cacheSessionData[BusinessEntity](sessionIdStr, BusinessEntityId, Overseas)

        val res = await(buildClient(pageUrl).post(Map("value" -> changingLegalEntityOfBusinessKey)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.RegReasonResolverController.resolve.url)
      }

      s"redirect to Taxable Supplies Page for an overseas user selecting sellingGoodsAndServices" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        cacheSessionData[Boolean](sessionIdStr, FixedEstablishmentId, false)
        cacheSessionData[BusinessEntity](sessionIdStr, BusinessEntityId, Overseas)

        val res = await(buildClient(pageUrl).post(Map("value" -> sellingGoodsAndServicesKey)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaxableSuppliesInUkController.onPageLoad.url)
      }

      s"redirect to Reg Reason Resolver for an established overseas user selecting sellingGoodsAndServices" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        cacheSessionData[Boolean](sessionIdStr, FixedEstablishmentId, true)
        cacheSessionData[BusinessEntity](sessionIdStr, BusinessEntityId, Overseas)

        val res = await(buildClient(pageUrl).post(Map("value" -> sellingGoodsAndServicesKey)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.RegReasonResolverController.resolve.url)
      }

      "clear down threshold/togc data if reg reason is changed" in new Setup {
        stubSuccessfulLogin()
        stubAudits()
        stubS4LGetNothing(testRegId)

        cacheSessionData[Boolean](sessionIdStr, FixedEstablishmentId, true)
        cacheSessionData[BusinessEntity](sessionIdStr, BusinessEntityId, UKCompany)
        cacheSessionData[RegistrationReason](sessionIdStr, RegistrationReasonId, SellingGoodsAndServices)
        cacheSessionData[ConditionalDateFormElement](sessionIdStr, ThresholdInTwelveMonthsId, ConditionalDateFormElement(true, Some(LocalDate.now())))
        cacheSessionData[ConditionalDateFormElement](sessionIdStr, ThresholdNextThirtyDaysId, ConditionalDateFormElement(true, Some(LocalDate.now())))
        cacheSessionData[ConditionalDateFormElement](sessionIdStr, ThresholdNextThirtyDaysId, ConditionalDateFormElement(true, Some(LocalDate.now())))
        cacheSessionData[Boolean](sessionIdStr, VoluntaryRegistrationId, true)
        cacheSessionData[DateFormElement](sessionIdStr, DateOfBusinessTransferId, DateFormElement(LocalDate.now()))
        cacheSessionData[String](sessionIdStr, PreviousBusinessNameId, "test")
        cacheSessionData[String](sessionIdStr, VATNumberId, "test")
        cacheSessionData[Boolean](sessionIdStr, KeepOldVrnId, true)
        cacheSessionData[Boolean](sessionIdStr, TaxableSuppliesInUkId, true)
        cacheSessionData[DateFormElement](sessionIdStr, ThresholdTaxableSuppliesId, DateFormElement(LocalDate.now()))

        val res = await(buildClient(pageUrl).post(Map("value" -> ukEstablishedOverseasExporterKey)))

        List(
          ThresholdInTwelveMonthsId, ThresholdNextThirtyDaysId, ThresholdNextThirtyDaysId, VoluntaryRegistrationId,
          DateOfBusinessTransferId, PreviousBusinessNameId, VATNumberId, KeepOldVrnId, TermsAndConditionsId,
          TaxableSuppliesInUkId, ThresholdTaxableSuppliesId
        ).foreach(id =>
          verifySessionCacheData(sessionIdStr, id, None)
        )

        res.status mustBe SEE_OTHER
      }
    }
    "the user doesn't answer" must {
      "return BAD_REQUEST" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map[String, String]()))

        res.status mustBe BAD_REQUEST
      }
    }
  }
}
