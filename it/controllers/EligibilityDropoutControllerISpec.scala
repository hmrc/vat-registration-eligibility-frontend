package controllers

import helpers.{IntegrationSpecBase, TrafficManagementStub}
import identifiers.{AgriculturalFlatRateSchemeId, BusinessEntityId, VATExceptionKickoutId}
import models.{Draft, RegistrationInformation, VatReg}
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames
import views.html.{agriculturalDropout, internationalActivityDropout, VatDivisionDropout}

import java.time.LocalDate

class EligibilityDropoutControllerISpec extends IntegrationSpecBase with TrafficManagementStub {

  val pageUrl = "/cant-register"
  val internationalActivitiesUrl = "/errors/business-activities-next-12-months"

  def cantRegisterUrl(id: String) = s"$pageUrl/$id"

  "GET /cant-register/agriculturalFlatRateScheme" must {
    "return OK" in new Setup {
      stubSuccessfulLogin()
      stubAudits()

      val view = app.injector.instanceOf[agriculturalDropout]
      val res = await(buildClient(cantRegisterUrl(AgriculturalFlatRateSchemeId.toString)).get)

      res.status mustBe OK
      res.body mustBe view()(request(cantRegisterUrl(AgriculturalFlatRateSchemeId.toString)), messages, appConfig).toString()
    }
  }

  "GET /cant-register/vatExceptionKickout" must {
    "redirect to the apply in writing page" in new Setup {
      stubSuccessfulLogin()
      stubAudits()

      val res = await(buildClient(cantRegisterUrl(VATExceptionKickoutId.toString)).get)

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(appConfig.VATWriteInURL)
    }
  }

  "GET /cant-register/businessEntity" must {
    "return OK" in new Setup {
      stubSuccessfulLogin()
      stubAudits()

      val view = app.injector.instanceOf[VatDivisionDropout]
      val res = await(buildClient(cantRegisterUrl(BusinessEntityId.toString)).get)

      res.status mustBe OK
      res.body mustBe view()(request(cantRegisterUrl(BusinessEntityId.toString)), messages, appConfig).toString()
    }
  }

  "GET /cant-register/???" must {
    "return OK" in new Setup {
      stubSuccessfulLogin()
      stubAudits()

      val res = await(buildClient(cantRegisterUrl("something-else")).get)

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(appConfig.otrsUrl)
    }
  }

  "GET /errors/business-activities-next-12-month" must {
    "return OK" in new Setup {
      stubSuccessfulLogin()
      stubAudits()

      val view = app.injector.instanceOf[internationalActivityDropout]
      val res = await(buildClient(internationalActivitiesUrl).get)

      res.status mustBe OK
      res.body mustBe view()(request(internationalActivitiesUrl), messages, appConfig).toString()
    }
  }

  "POST /cant-register" must {
    "redirect to the legacy OTRS journey" in new Setup {
      stubSuccessfulLogin()
      stubAudits()
      stubUpsertRegistrationInformation(testRegId)(RegistrationInformation(testInternalId, testRegId, Draft, Some(LocalDate.now), VatReg))

      val res = await(buildClient(pageUrl).post(Json.obj()))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(appConfig.otrsUrl)
    }
  }

}
