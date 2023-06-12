package controllers

import helpers.IntegrationSpecBase
import identifiers.BusinessEntityId
import models.BusinessEntity._
import org.jsoup.Jsoup
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class BusinessEntityOtherControllerISpec extends IntegrationSpecBase {

  val pageUrl: String = "/business-entity-other"
  val options = Seq(charitableIncorporatedOrganisationKey, nonIncorporatedTrustKey, registeredSocietyKey, unincorporatedAssociationKey, divisionKey)

  "GET /business-entity-other" when {
    "an answer exists for the page" when {
      options.foreach { option =>
        s"the answer is $option" must {
          "return OK with the UKCompany option selected" in new Setup {
            stubSuccessfulLogin()
            stubAudits()

            cacheSessionData(sessionIdStr, BusinessEntityId, option)

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

  "POST /business-entity-other" when {
    "the option is CIO" must {
      "redirect to the AFRS page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map("value" -> charitableIncorporatedOrganisationKey)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.AgriculturalFlatRateSchemeController.onPageLoad.url)
      }
    }
    "the option is Trust" must {
      "redirect to the AFRS page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map("value" -> nonIncorporatedTrustKey)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.AgriculturalFlatRateSchemeController.onPageLoad.url)
      }
    }
    "the option is Registered Society" must {
      "redirect to the AFRS page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map("value" -> registeredSocietyKey)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.AgriculturalFlatRateSchemeController.onPageLoad.url)
      }
    }
    "the option is Unincorporated Association" must {
      "redirect to the AFRS page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map("value" -> unincorporatedAssociationKey)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.AgriculturalFlatRateSchemeController.onPageLoad.url)
      }
    }
    "the option is Division" must {
      "redirect to the Eligibility Dropout page" in new Setup {
        stubSuccessfulLogin()
        stubAudits()

        val res = await(buildClient(pageUrl).post(Map("value" -> divisionKey)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.EligibilityDropoutController.onPageLoad(BusinessEntityId.toString).url)
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

