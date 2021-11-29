package helpers

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.RegistrationInformation
import play.api.libs.json.Json

trait TrafficManagementStub {

  private def trafficManagementUrl(regId: String) = s"/vatreg/traffic-management/$regId/allocate"

  private def getRegInfoURl(regId: String) = s"/vatreg/traffic-management/$regId/reg-info"

  def stubAllocation(regId: String)(status: Int): StubMapping =
    stubFor(post(urlMatching(trafficManagementUrl(regId)))
      .willReturn(aResponse.withStatus(status)))

  def stubGetRegistrationInformation(regId: String)(status: Int, body: Option[RegistrationInformation]): StubMapping =
    stubFor(get(urlMatching(getRegInfoURl(regId)))
      .willReturn(aResponse.withStatus(status).withBody(body.fold("")(Json.toJson(_).toString))))

  def stubUpsertRegistrationInformation(regId: String)(body: RegistrationInformation): StubMapping =
    stubFor(put(urlMatching(getRegInfoURl(regId)))
      .willReturn(aResponse.withBody(Json.toJson(body).toString)))
}
