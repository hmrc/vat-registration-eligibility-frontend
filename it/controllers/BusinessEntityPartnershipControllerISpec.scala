package controllers

import helpers.IntegrationSpecBase
import identifiers.BusinessEntityId
import models.BusinessEntity._
import org.jsoup.Jsoup
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class BusinessEntityPartnershipControllerISpec extends IntegrationSpecBase {

  val pageUrl = "/business-entity-partnership"

  val options = Seq(
    generalPartnershipKey,
    limitedPartnershipKey,
    scottishPartnershipKey,
    scottishLimitedPartnershipKey,
    limitedLiabilityPartnershipKey
  )

  "GET /business-entity-partnership" when {
    "an answer exists for the page already" when {
      options.foreach { option =>
        s"the option is $option" must {
          "return OK with the form pre-populated" in new Setup {
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

  "POST /business-entity-partnership" when {
    "the answer is General Partnership" must {
      "redirect to the AFRS page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map("value" -> generalPartnershipKey)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.AgriculturalFlatRateSchemeController.onPageLoad.url)
      }
    }
    "the answer is Limited Partnership" must {
      "redirect to the AFRS page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map("value" -> limitedPartnershipKey)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.AgriculturalFlatRateSchemeController.onPageLoad.url)
      }
    }
    "the answer is Scottish Partnership" must {
      "redirect to the AFRS page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map("value" -> scottishPartnershipKey)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.AgriculturalFlatRateSchemeController.onPageLoad.url)
      }
    }
    "the answer is Scottish Limited Partnership" must {
      "redirect to the AFRS page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map("value" -> scottishLimitedPartnershipKey)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.AgriculturalFlatRateSchemeController.onPageLoad.url)
      }
    }
    "the answer is Limited Liability Partnership" must {
      "redirect to the AFRS page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map("value" -> limitedLiabilityPartnershipKey)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.AgriculturalFlatRateSchemeController.onPageLoad.url)
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
