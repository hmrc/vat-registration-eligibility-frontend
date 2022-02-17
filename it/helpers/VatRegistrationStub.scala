package helpers

import com.github.tomakehurst.wiremock.client.WireMock.{ok, patch, stubFor, urlMatching}
import identifiers._
import play.api.libs.json.{JsBoolean, JsString, Json}

trait VatRegistrationStub {

  val testEligibilityDataFull = Json.obj(
    ThresholdInTwelveMonthsId.toString -> Json.obj("value" -> JsBoolean(false)),
    ThresholdNextThirtyDaysId.toString -> Json.obj("value" -> JsBoolean(false)),
    ThresholdPreviousThirtyDaysId.toString -> Json.obj("value" -> JsBoolean(false)),
    VoluntaryRegistrationId.toString -> JsBoolean(true),
    TurnoverEstimateId.toString -> Json.obj("amount" -> JsString("50000")),
    InternationalActivitiesId.toString -> JsBoolean(false),
    InvolvedInOtherBusinessId.toString -> JsBoolean(false),
    VoluntaryRegistrationId.toString -> JsBoolean(true),
    VATExemptionId.toString -> JsBoolean(false),
    ZeroRatedSalesId.toString -> JsBoolean(true),
    RegisteringBusinessId.toString -> JsBoolean(true),
    NinoId.toString -> JsBoolean(true),
    AgriculturalFlatRateSchemeId.toString -> JsBoolean(false),
    RacehorsesId.toString -> JsBoolean(false)
  )

  def stubSaveEligibilityData(regId: String) = {
    stubFor(
      patch(urlMatching(s"/vatreg/$regId/eligibility-data"))
        .willReturn(ok(testEligibilityDataFull.toString()))
    )
  }

}
