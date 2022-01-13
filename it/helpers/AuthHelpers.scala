package helpers

import akka.util.Timeout
import com.github.tomakehurst.wiremock.client.WireMock._
import models.CurrentProfile
import play.api.Application
import play.api.http.HeaderNames
import play.api.libs.json.{JsArray, Json}
import play.api.test.Helpers.await
import services.SessionService
import support.SessionCookieBaker
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.ExtraSessionKeys

import scala.concurrent.duration._

trait AuthHelper {

  private[helpers] val defaultUser = "/foo/bar"

  val authSessionId = "sessionId"
  val testRegId = "testRegId"
  val testInternalId = "testInternalId"

  private def cookieData(additionalData: Map[String, String], userId: String = defaultUser): Map[String, String] = {
    Map(
      SessionKeys.sessionId -> authSessionId,
      ExtraSessionKeys.userId -> userId,
      ExtraSessionKeys.token -> "token",
      ExtraSessionKeys.authProvider -> "GGW",
      SessionKeys.lastRequestTimestamp -> new java.util.Date().getTime.toString
    ) ++ additionalData
  }

  def getSessionCookie(additionalData: Map[String, String] = Map(), userId: String = defaultUser) = {
    SessionCookieBaker.cookieValue(cookieData(additionalData, userId))
  }

  def stubSuccessfulLogin(userId: String = defaultUser, withSignIn: Boolean = false, enrolments: JsArray = Json.arr()) = {
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
          "internalId" -> testInternalId,
          "allEnrolments" -> enrolments
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

  class SessionTest(app: Application, cacheMap: CacheMap = CacheMap(id = authSessionId, data = Map()))(implicit hc: HeaderCarrier) {
    val timeout: Timeout = 5.seconds
    val profileKey = "CurrentProfile"
    val dataKey = "data"

    val sessionService = app.injector.instanceOf[SessionService]

    await(sessionService.save(cacheMap))(timeout)
    await(sessionService.save(profileKey, CurrentProfile(testRegId)))(timeout)
  }

}