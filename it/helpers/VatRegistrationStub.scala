package helpers

import com.github.tomakehurst.wiremock.client.WireMock.{ok, patch, stubFor, urlMatching}
import identifiers._
import play.api.libs.json.{JsBoolean, Json}

trait VatRegistrationStub {

  val testEligibilityDataFull = Json.obj(
    ThresholdInTwelveMonthsId.toString -> Json.obj("value" -> JsBoolean(false)),
    ThresholdNextThirtyDaysId.toString -> Json.obj("value" -> JsBoolean(false)),
    ThresholdPreviousThirtyDaysId.toString -> Json.obj("value" -> JsBoolean(false)),
    VoluntaryRegistrationId.toString -> JsBoolean(true),
    InternationalActivitiesId.toString -> JsBoolean(false),
    VoluntaryRegistrationId.toString -> JsBoolean(true),
    RegisteringBusinessId.toString -> JsBoolean(true),
    AgriculturalFlatRateSchemeId.toString -> JsBoolean(false)
  )

  def stubSaveEligibilityData(regId: String) = {
    stubFor(
      patch(urlMatching(s"/vatreg/$regId/eligibility-data"))
        .willReturn(ok(testEligibilityDataFull.toString()))
    )
  }

}
