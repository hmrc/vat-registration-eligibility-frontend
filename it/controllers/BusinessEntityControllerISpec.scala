package controllers

import helpers.IntegrationSpecBase
import identifiers.{BusinessEntityId, FixedEstablishmentId}
import models.BusinessEntity.{otherKey, partnershipKey, soleTraderKey, ukCompanyKey}
import models._
import org.jsoup.Jsoup
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class BusinessEntityControllerISpec extends IntegrationSpecBase {

  val pageUrl: String = "/business-entity"
  val options = Seq(ukCompanyKey, soleTraderKey, partnershipKey, otherKey)

  "GET /business-entity" when {
    "an answer exists for the page" when {
      "the user's business entity is NETP and they have no fixed establishment" when {
        "redirect to the Business Entity Overseas page" in new Setup {
          stubSuccessfulLogin()
          stubAudits()

          cacheSessionData[BusinessEntity](sessionId, BusinessEntityId, NETP)
          cacheSessionData(sessionId, FixedEstablishmentId, false)

          val res = await(buildClient(pageUrl).get)

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessEntityOverseasController.onPageLoad.url)
        }
      }
      "the user's business entity is NETP and they have a fixed establishment" when {
        "show the page with no prepop" in new Setup {
          stubSuccessfulLogin()
          stubAudits()

          cacheSessionData[BusinessEntity](sessionId, BusinessEntityId, NETP)
          cacheSessionData(sessionId, FixedEstablishmentId, true)

          val res = await(buildClient(pageUrl).get)
          val doc = Jsoup.parse(res.body)

          res.status mustBe OK
          doc.radioIsSelected(otherKey) mustBe false
          doc.radioIsSelected(partnershipKey) mustBe false
          doc.radioIsSelected(ukCompanyKey) mustBe false
          doc.radioIsSelected(soleTraderKey) mustBe false
        }
      }
      "the user's business entity is Non UK and they have no fixed establishment" when {
        "redirect to the Business Entity Overseas page" in new Setup {
          stubSuccessfulLogin()
          stubAudits()

          cacheSessionData[BusinessEntity](sessionId, BusinessEntityId, Overseas)
          cacheSessionData(sessionId, FixedEstablishmentId, false)

          val res = await(buildClient(pageUrl).get)

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessEntityOverseasController.onPageLoad.url)
        }
      }
      "the user's business entity is Non UK and they have a fixed establishment in UK/Man" when {
        "show the page with Other preselected" in new Setup {
          stubSuccessfulLogin()
          stubAudits()

          cacheSessionData[BusinessEntity](sessionId, BusinessEntityId, Overseas)
          cacheSessionData(sessionId, FixedEstablishmentId, true)

          val res = await(buildClient(pageUrl).get)
          val doc = Jsoup.parse(res.body)

          res.status mustBe OK
          doc.radioIsSelected(otherKey) mustBe true
        }
      }
      "the user's business entity is a Partnership entity" must {
        "show the page with partnership preselected" in new Setup {
          stubSuccessfulLogin()
          stubAudits()

          cacheSessionData[BusinessEntity](sessionId, BusinessEntityId, GeneralPartnership)

          val res = await(buildClient(pageUrl).get)
          val doc = Jsoup.parse(res.body)

          res.status mustBe OK
          doc.radioIsSelected(partnershipKey) mustBe true
        }
      }
      "the user's business entity is an 'Other' entity" must {
        "show the page with other preselected" in new Setup {
          stubSuccessfulLogin()
          stubAudits()

          cacheSessionData[BusinessEntity](sessionId, BusinessEntityId, RegisteredSociety)

          val res = await(buildClient(pageUrl).get)
          val doc = Jsoup.parse(res.body)

          res.status mustBe OK
          doc.radioIsSelected(otherKey) mustBe true
        }
      }
      options.foreach { option =>
        s"the answer is $option" must {
          "return OK with the correct option selected" in new Setup {
            stubSuccessfulLogin()
            stubAudits()

            cacheSessionData(sessionId, BusinessEntityId, option)

            val res = await(buildClient(pageUrl).get)
            val doc = Jsoup.parse(res.body)

            res.status mustBe OK
            doc.radioIsSelected(option) mustBe true

            for (unselectedOption <- options.filterNot(_ == option)) {
              doc.radioIsSelected(unselectedOption) mustBe false
            }
          }
        }
      }
    }
    "no answer exists for the page" must {
      "return OK with an empty form" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).get)
        val doc = Jsoup.parse(res.body)

        res.status mustBe OK

        for (unselectedOption <- options) {
          doc.radioIsSelected(unselectedOption) mustBe false
        }
      }
    }
  }

  "POST /business-entity" when {
    "the option is UKCompany" must {
      "redirect to the AFRS page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map("value" -> ukCompanyKey)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.AgriculturalFlatRateSchemeController.onPageLoad.url)
      }
    }
    "the option is SoleTrader" must {
      "redirect to the AFRS page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map("value" -> soleTraderKey)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.AgriculturalFlatRateSchemeController.onPageLoad.url)
      }
    }
    "the option is Partnership" must {
      "redirect to the BusinessEntityPartnership page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map("value" -> partnershipKey)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.BusinessEntityPartnershipController.onPageLoad.url)
      }
    }
    "the option is Other" must {
      "redirect to the BusinessEntityOther page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map("value" -> otherKey)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.BusinessEntityOtherController.onPageLoad.url)
      }
    }
    "the user doesn't select an option" must {
      "return BAD_REQUEST" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map[String, String]()))

        res.status mustBe BAD_REQUEST
      }
    }
  }

}
