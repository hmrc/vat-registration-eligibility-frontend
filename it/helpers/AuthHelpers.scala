package helpers

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.http.HeaderNames
import play.api.libs.json.Json
import support.SessionCookieBaker
import uk.gov.hmrc.http.SessionKeys
import utils.ExtraSessionKeys

trait AuthHelper {

  private[helpers] val defaultUser = "/foo/bar"

  val sessionId = "session-ac4ed3e7-dbc3-4150-9574-40771c4285c1"
  val testRegId = "testRegId"

  private def cookieData(additionalData: Map[String, String], userId: String = defaultUser): Map[String, String] = {
    Map(
      SessionKeys.sessionId -> sessionId,
      ExtraSessionKeys.userId -> userId,
      ExtraSessionKeys.token -> "token",
      ExtraSessionKeys.authProvider -> "GGW",
      SessionKeys.lastRequestTimestamp -> new java.util.Date().getTime.toString
    ) ++ additionalData
  }

  def getSessionCookie(additionalData: Map[String, String] = Map(), userId: String = defaultUser) = {
    SessionCookieBaker.cookieValue(cookieData(additionalData, userId))
  }

  def stubSuccessfulLogin(userId: String = defaultUser, withSignIn: Boolean = false) = {
    if (withSignIn) {
      val continueUrl = "/wibble"
      stubFor(get(urlEqualTo(s"/gg/sign-in?continue=${continueUrl}"))
        .willReturn(aResponse()
          .withStatus(303)
          .withHeader(HeaderNames.SET_COOKIE, getSessionCookie())
          .withHeader(HeaderNames.LOCATION, continueUrl)))
    }

    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(ok(Json.obj(
          "optionalCredentials" -> Json.obj(
            "providerId" -> "testProviderID",
            "providerType" -> "GovernmentGateway"
          ),
          "internalId" -> "testInternalId"
        ).toString())))
  }


  def stubAudits() = {
    stubFor(post(urlMatching("/write/audit"))
      .willReturn(
        aResponse().
          withStatus(204)
      )
    )

    stubFor(post(urlMatching("/write/audit/merged"))
      .willReturn(
        aResponse().
          withStatus(204)
      )
    )
  }

  def stubSuccessfulRegIdGet(): Unit = {
    stubFor(
      get(
        urlMatching("/vatreg/scheme")
      ).willReturn(ok(s"""{"registrationId":"$testRegId"}""")))
  }
}