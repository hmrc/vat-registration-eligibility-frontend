
package controllers

import common.enums.CacheKeys
import helpers.RequestsFinder
import models.view.VoluntaryRegistration.{REGISTER_NO, REGISTER_YES}
import models.view.TaxableTurnover.{TAXABLE_NO, TAXABLE_YES}
import models.view.{ExpectationOverThresholdView, OverThresholdView, TaxableTurnover, Threshold, VoluntaryRegistration, VoluntaryRegistrationReason}
import models.view.VoluntaryRegistrationReason.{NEITHER, SELLS}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.http.HeaderNames
import play.api.libs.json.{JsBoolean, JsString, Json}
import support.AppAndStubs


class ThresholdControllerISpec extends PlaySpec with AppAndStubs with RequestsFinder with ScalaFutures {
  "GET Taxable Turnover page" should {
    "return 200" when {
      "user is authorised and all conditions are fulfilled" in {
        val s4lData = Threshold(None, None, None, None, None)

        given()
          .user.isAuthorised
          .currentProfile.withProfile()
          .audit.writesAudit()
          .s4lContainer.isEmpty
          .vatScheme.hasNoData("threshold")
          .s4lContainer.isUpdatedWith(CacheKeys.Threshold, s4lData)

        val response = buildClient("/make-more-taxable-sales").get()
        whenReady(response)(_.status) mustBe 200
      }
    }
  }

  "POST Taxable Turnover page" should {
    "return 303" when {
      "user choose taxable turnover YES value and save data to backend" in {
        val startedS4LData = Threshold(Some(TaxableTurnover(TAXABLE_NO)), None, None, None, None)
        val json = Json.parse(
          s"""
             |{
             |  "mandatoryRegistration": true
             |}
           """.stripMargin)

        given()
          .user.isAuthorised
          .currentProfile.withProfile()
          .s4lContainer.contains(CacheKeys.Threshold, startedS4LData)
          .s4lContainer.cleared
          .vatScheme.patched("threshold", json)
          .audit.writesAudit()

        val response = buildClient("/make-more-taxable-sales").post(Map("taxableTurnoverRadio" -> Seq(TAXABLE_YES)))
        whenReady(response) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some("/vat-uri/who-is-registering-the-company-for-vat")

          val json = getPATCHRequestJsonBody(s"/vatreg/1/threshold")
          (json \ "mandatoryRegistration").as[JsBoolean].value mustBe true
        }
      }

      "user choose taxable turnover NO value and save to S4L" in {
        val startedS4LData = Threshold(Some(TaxableTurnover(TAXABLE_YES)), None, None, None, None)
        val s4lData = Threshold(Some(TaxableTurnover(TAXABLE_NO)), None, None, None, None)

        given()
          .user.isAuthorised
          .currentProfile.withProfile()
          .s4lContainer.contains(CacheKeys.Threshold, startedS4LData)
          .s4lContainer.isUpdatedWith(CacheKeys.Threshold, s4lData)
          .audit.writesAudit()

        val response = buildClient("/make-more-taxable-sales").post(Map("taxableTurnoverRadio" -> Seq(TAXABLE_NO)))
        whenReady(response) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VoluntaryRegistrationController.show().url)
        }
      }
    }
  }

  "GET Over Threshold page" should {
    "return 200" when {
      "when user is authorised and has a date of incorporation" in {
        val s4lData = Threshold(None, None, None, None, None)

        given()
          .user.isAuthorised
          .currentProfile.withProfileAndIncorpDate()
          .s4lContainer.isEmpty
          .vatScheme.hasNoData("threshold")
          .s4lContainer.isUpdatedWith(CacheKeys.Threshold, s4lData)
          .audit.writesAudit()

        val response = buildClient("/vat-taxable-turnover-gone-over").get()
        whenReady(response)(_.status) mustBe 200
      }
    }
  }

  "POST Over Threshold page" should{
    "return 303" when {
      "when the request is valid" in {
        val s4lData = Threshold(None, None, None, None, None)
        val s4lDataUpdated = Threshold(None, None, None, Some(OverThresholdView(selection = false, None)), None)

        given()
          .user.isAuthorised
          .currentProfile.withProfileAndIncorpDate()
          .audit.writesAudit()
          .s4lContainer.contains(CacheKeys.Threshold, s4lData)
          .s4lContainer.isUpdatedWith(CacheKeys.Threshold, s4lDataUpdated)

        val response = buildClient("/vat-taxable-turnover-gone-over").post(Map("overThresholdRadio" ->Seq("false")))
        whenReady(response) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdController.expectationOverShow().url)
        }
      }

      "the request is valid with overThreshold value set to true AND save to backend" in {
        val s4lData = Threshold(
          None,
          Some(VoluntaryRegistration(REGISTER_YES)),
          None,
          Some(OverThresholdView(selection = false, None)),
          Some(ExpectationOverThresholdView(selection = false, None))
        )

        val json = Json.parse(
          s"""
             |{
             |  "mandatoryRegistration": true,
             |  "overThresholdDate": "2016-08-31"
             |}
           """.stripMargin)

        given()
          .user.isAuthorised
          .currentProfile.withProfileAndIncorpDate()
          .audit.writesAudit()
          .s4lContainer.contains(CacheKeys.Threshold, s4lData)
          .vatScheme.patched("threshold", json)
          .s4lContainer.cleared

        val response = buildClient("/vat-taxable-turnover-gone-over").post(
          Map("overThresholdRadio" -> Seq("true"),
            "overThreshold.day" -> Seq("06"),
            "overThreshold.month" -> Seq("08"),
            "overThreshold.year" -> Seq("2016")
          )
        )

        whenReady(response) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdController.expectationOverShow().url)


          val json = getPATCHRequestJsonBody(s"/vatreg/1/threshold")
          (json \ "mandatoryRegistration").as[JsBoolean].value mustBe true
          (json \ "overThresholdDate").as[JsString].value mustBe "2016-08-31"
          (json \ "expectedOverThresholdDate").validateOpt[JsString].get mustBe None
          (json \ "voluntaryReason").validateOpt[JsString].get mustBe None
        }
      }
    }
  }

  "GET Expected Over Threshold page" should {
    "return 200" when {
      "user is authorised and has a date of incorporation" in {
        val s4lData = Threshold(None, None, None, Some(OverThresholdView(selection = false, None)), None)

        given()
          .user.isAuthorised
          .currentProfile.withProfileAndIncorpDate()
          .s4lContainer.contains(CacheKeys.Threshold, s4lData)
          .audit.writesAudit()

        val response = buildClient("/go-over-threshold-period").get()
        whenReady(response)(_.status) mustBe 200
      }
    }
  }

  "POST Expectation Over Threshold page" should{
    "return 303" when {
      "the request is valid" in {
        val s4lData = Threshold(None, None, None, Some(OverThresholdView(selection = false, None)), None)
        val s4lDataUpdated = Threshold(
          None,
          None,
          None,
          Some(OverThresholdView(selection = false, None)),
          Some(ExpectationOverThresholdView(selection = false, None))
        )

        given()
          .user.isAuthorised
          .currentProfile.withProfileAndIncorpDate()
          .audit.writesAudit()
          .s4lContainer.contains(CacheKeys.Threshold, s4lData)
          .s4lContainer.isUpdatedWith(CacheKeys.Threshold, s4lDataUpdated)

        val response = buildClient("/go-over-threshold-period").post(Map("expectationOverThresholdRadio" -> Seq("false")))
        whenReady(response) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdSummaryController.show().url)
        }
      }

      "the request is valid with expectedOverThreshold value set to true AND save to backend" in {
        val s4lData = Threshold(
          None,
          Some(VoluntaryRegistration(REGISTER_YES)),
          None,
          Some(OverThresholdView(selection = false, None)),
          Some(ExpectationOverThresholdView(selection = false, None))
        )

        val json = Json.parse(
          s"""
             |{
             |  "mandatoryRegistration": true,
             |  "expectedOverThresholdDate": "2016-08-06"
             |}
           """.stripMargin)

        given()
          .user.isAuthorised
          .currentProfile.withProfileAndIncorpDate()
          .audit.writesAudit()
          .s4lContainer.contains(CacheKeys.Threshold, s4lData)
          .vatScheme.patched("threshold", json)
          .s4lContainer.cleared

        val response = buildClient("/go-over-threshold-period").post(Map("expectationOverThresholdRadio" -> Seq("true"),
                                                                             "expectationOverThreshold.day" -> Seq("06"),
                                                                             "expectationOverThreshold.month" -> Seq("08"),
                                                                             "expectationOverThreshold.year" -> Seq("2016")))
        whenReady(response) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdSummaryController.show().url)


          val json = getPATCHRequestJsonBody(s"/vatreg/1/threshold")
          (json \ "mandatoryRegistration").as[JsBoolean].value mustBe true
          (json \ "expectedOverThresholdDate").as[JsString].value mustBe "2016-08-06"
          (json \ "overThresholdDate").validateOpt[JsString].get mustBe None
          (json \ "voluntaryReason").validateOpt[JsString].get mustBe None
        }
      }

      "the data is complete and saved into backend" in {
        val s4lData = Threshold(
          None,
          Some(VoluntaryRegistration(REGISTER_YES)),
          Some(VoluntaryRegistrationReason(SELLS)),
          Some(OverThresholdView(selection = false)),
          Some(ExpectationOverThresholdView(selection = false))
        )

        val json = Json.parse(
          s"""
             |{
             |  "mandatoryRegistration": false,
             |  "voluntaryReason": "$SELLS"
             |}
           """.stripMargin)

        given()
          .user.isAuthorised
          .currentProfile.withProfileAndIncorpDate()
          .audit.writesAudit()
          .s4lContainer.contains(CacheKeys.Threshold, s4lData)
          .vatScheme.patched("threshold", json)
          .s4lContainer.cleared

        val response = buildClient("/go-over-threshold-period").post(Map("expectationOverThresholdRadio" -> Seq("false")))
        whenReady(response) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ThresholdSummaryController.show().url)

          val json = getPATCHRequestJsonBody(s"/vatreg/1/threshold")
          (json \ "mandatoryRegistration").as[JsBoolean].value mustBe false
          (json \ "voluntaryReason").as[JsString].value mustBe SELLS
          (json \ "overThresholdDate").validateOpt[JsString].get mustBe None
          (json \ "expectedOverThresholdDate").validateOpt[JsString].get mustBe None
        }
      }
    }
  }

  "GET Voluntary Registration page" should {
    "return 200" when {
      "the request is valid" in {
        val s4lData = Threshold(
          None,
          None,
          None,
          Some(OverThresholdView(selection = false)),
          Some(ExpectationOverThresholdView(selection = false))
        )

        given()
          .user.isAuthorised
          .currentProfile.withProfile()
          .audit.writesAudit()
          .s4lContainer.contains(CacheKeys.Threshold, s4lData)

        val response = buildClient("/register-voluntarily").get()
        whenReady(response)(_.status) mustBe 200
      }
    }
  }

  "POST Voluntary Registration page" should {
    "return 303" when {
      "the user choose value YES and save to S4L" in {
        val s4lData = Threshold(
          None,
          None,
          None,
          Some(OverThresholdView(selection = false)),
          Some(ExpectationOverThresholdView(selection = false))
        )

        val s4lDataUpdated = Threshold(
          None,
          Some(VoluntaryRegistration(REGISTER_YES)),
          None,
          Some(OverThresholdView(selection = false)),
          Some(ExpectationOverThresholdView(selection = false))
        )

        given()
          .user.isAuthorised
          .currentProfile.withProfile()
          .audit.writesAudit()
          .s4lContainer.contains(CacheKeys.Threshold, s4lData)
          .s4lContainer.isUpdatedWith(CacheKeys.Threshold, s4lDataUpdated)

        val response = buildClient("/register-voluntarily").post(Map("voluntaryRegistrationRadio" -> Seq(REGISTER_YES)))
        whenReady(response) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.VoluntaryRegistrationReasonController.show().url)
        }
      }

      "the user choose value NO and save in S4L" in {
        val s4lData = Threshold(
          None,
          None,
          None,
          Some(OverThresholdView(selection = false)),
          Some(ExpectationOverThresholdView(selection = false))
        )

        val s4lDataUpdated = Threshold(
          None,
          Some(VoluntaryRegistration(REGISTER_NO)),
          None,
          Some(OverThresholdView(selection = false)),
          Some(ExpectationOverThresholdView(selection = false))
        )

        given()
          .user.isAuthorised
          .currentProfile.withProfile()
          .audit.writesAudit()
          .s4lContainer.contains(CacheKeys.Threshold, s4lData)
          .s4lContainer.isUpdatedWith(CacheKeys.Threshold, s4lDataUpdated)

        val response = buildClient("/register-voluntarily").post(Map("voluntaryRegistrationRadio" -> Seq(REGISTER_NO)))
        whenReady(response) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some (controllers.routes.VoluntaryRegistrationController.showChoseNoToVoluntary().url)
        }
      }
    }
  }

  "GET Voluntary Registration Reason page" should {
    "return 200" when {
      "the request is valid" in {
        val s4lData = Threshold(
          None,
          Some(VoluntaryRegistration(REGISTER_YES)),
          None,
          Some(OverThresholdView(selection = false)),
          Some(ExpectationOverThresholdView(selection = false))
        )

        given()
          .user.isAuthorised
          .currentProfile.withProfile()
          .audit.writesAudit()
          .s4lContainer.contains(CacheKeys.Threshold, s4lData)

        val response = buildClient("/applies-company").get()
        whenReady(response)(_.status) mustBe 200
      }
    }
  }

  "POST Voluntary Registration Reason page" should {
    "return 303" when {
      val s4lData = Threshold(
        Some(TaxableTurnover(TAXABLE_NO)),
        Some(VoluntaryRegistration(REGISTER_YES)),
        None,
        None,
        None
      )

      "the user selects a valid reason" in {
        val json = Json.parse(
          s"""
             |{
             |  "mandatoryRegistration": false,
             |  "voluntaryReason": "$SELLS"
             |}
           """.stripMargin)

        given()
          .user.isAuthorised
          .currentProfile.withProfile()
          .audit.writesAudit()
          .s4lContainer.contains(CacheKeys.Threshold, s4lData)
          .vatScheme.patched("threshold", json)
          .s4lContainer.cleared

        val response = buildClient("/applies-company").post(Map("voluntaryRegistrationReasonRadio" -> Seq(SELLS)))
        whenReady(response) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some("/vat-uri/who-is-registering-the-company-for-vat")

          val json = getPATCHRequestJsonBody(s"/vatreg/1/threshold")
          (json \ "mandatoryRegistration").as[JsBoolean].value mustBe false
          (json \ "voluntaryReason").as[JsString].value mustBe SELLS
          (json \ "overThresholdDate").validateOpt[JsString].get mustBe None
          (json \ "expectedOverThresholdDate").validateOpt[JsString].get mustBe None
        }
      }

      "the user selects an invalid reason" in {
        val json = Json.parse(
          s"""
             |{
             |  "mandatoryRegistration": false,
             |  "voluntaryReason": "$NEITHER"
             |}
           """.stripMargin)

        given()
          .user.isAuthorised
          .currentProfile.withProfile()
          .audit.writesAudit()
          .s4lContainer.contains(CacheKeys.Threshold, s4lData)
          .vatScheme.patched("threshold", json)
          .s4lContainer.cleared

        val response = buildClient("/applies-company").post(Map("voluntaryRegistrationReasonRadio" -> Seq(NEITHER)))
        whenReady(response) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some("/vat-uri")

          val json = getPATCHRequestJsonBody(s"/vatreg/1/threshold")
          (json \ "mandatoryRegistration").as[JsBoolean].value mustBe false
          (json \ "voluntaryReason").as[JsString].value mustBe NEITHER
          (json \ "overThresholdDate").validateOpt[JsString].get mustBe None
          (json \ "expectedOverThresholdDate").validateOpt[JsString].get mustBe None
        }
      }
    }
  }
}
