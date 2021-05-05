
package helpers

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap

trait S4LStub {

  def s4LGetUrl(regId: String) = s"/save4later/vat-registration-eligibility-frontend/$regId"

  def s4LPutUrl(regId: String, key: String) = s"/save4later/vat-registration-eligibility-frontend/$regId/data/$key"

  def stubS4LGet(regId: String)(response: CacheMap): StubMapping =
    stubFor(get(urlMatching(s4LGetUrl(regId)))
      .willReturn(ok(Json.toJson(response).toString)))

  def stubS4LGetNothing(regId: String): StubMapping =
    stubFor(get(urlMatching(s4LGetUrl(regId)))
      .willReturn(aResponse().withStatus(NOT_FOUND)))

  def stubS4LSave[T](regId: String, key: String)(response: CacheMap): StubMapping =
    stubFor(put(urlMatching(s4LPutUrl(regId, key)))
      .willReturn(ok(Json.toJson(response).toString)))

}
