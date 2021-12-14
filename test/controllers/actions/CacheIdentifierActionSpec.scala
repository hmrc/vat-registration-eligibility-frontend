/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.actions

import base.SpecBase
import controllers.routes
import models.CurrentProfile
import models.requests.CacheIdentifierRequest
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.mvc._
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException, SessionId}

import scala.concurrent.{ExecutionContext, Future}

class CacheIdentifierActionSpec extends SpecBase {

  class Harness(authAction: CacheIdentifierAction,
                val parser: BodyParsers.Default)
               (implicit override val executionContext: ExecutionContext) extends CacheIdentifierAction with BaseController {

    val testInternalId = "testInternalId"
    val testRegId = "regId"
    val sessionId = "sessionId"

    def onPageLoad = authAction { request => Ok }

    override protected def controllerComponents: ControllerComponents = controllerComponents

    override def invokeBlock[A](request: Request[A], block: CacheIdentifierRequest[A] => Future[Result]): Future[Result] =
      block(CacheIdentifierRequest(request, testRegId, testInternalId))

    implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId)))

    "Auth Action" when {
      "the user hasn't started the journey" must {
        "redirect the user to the start of the journey" in {
          val authAction = new CacheIdentifierActionImpl(mockAuthConnector, frontendAppConfig, mockJourneyService, parser)
          val controller = new Harness(authAction, parser)

          when(mockAuthConnector.authorise(
            ArgumentMatchers.any,
            ArgumentMatchers.eq(Retrievals.internalId)
          )(
            ArgumentMatchers.any[HeaderCarrier],
            ArgumentMatchers.any[ExecutionContext]
          )).thenReturn(Future.successful(Some(testInternalId)))

          when(mockJourneyService.getProfile(ArgumentMatchers.eq(hc)))
            .thenReturn(Future.successful(None))

          val result = controller.onPageLoad(fakeRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result).get mustBe Some(s"${frontendAppConfig.vatRegFEURL}${frontendAppConfig.vatRegFEURI}")
        }
      }

      "the user hasn't logged in" must {
        "redirect the user to log in " in {
          val authAction = new CacheIdentifierActionImpl(new FakeFailingAuthConnector(new MissingBearerToken), frontendAppConfig, mockJourneyService, parser)
          val controller = new Harness(authAction, parser)
          val result = controller.onPageLoad(fakeRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
        }
      }

      "the user's session has expired" must {
        "redirect the user to log in " in {
          val authAction = new CacheIdentifierActionImpl(new FakeFailingAuthConnector(new BearerTokenExpired), frontendAppConfig, mockJourneyService, parser)
          val controller = new Harness(authAction, parser)
          val result = controller.onPageLoad(fakeRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
        }
      }

      "the user doesn't have sufficient enrolments" must {
        "redirect the user to the unauthorised page" in {
          val authAction = new CacheIdentifierActionImpl(new FakeFailingAuthConnector(new InsufficientEnrolments), frontendAppConfig, mockJourneyService, parser)
          val controller = new Harness(authAction, parser)
          val result = controller.onPageLoad(fakeRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
        }
      }

      "the user doesn't have sufficient confidence level" must {
        "redirect the user to the unauthorised page" in {
          val authAction = new CacheIdentifierActionImpl(new FakeFailingAuthConnector(new InsufficientConfidenceLevel), frontendAppConfig, mockJourneyService, parser)
          val controller = new Harness(authAction, parser)
          val result = controller.onPageLoad(fakeRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
        }
      }

      "the user used an unaccepted auth provider" must {
        "redirect the user to the unauthorised page" in {
          val authAction = new CacheIdentifierActionImpl(new FakeFailingAuthConnector(new UnsupportedAuthProvider), frontendAppConfig, mockJourneyService, parser)
          val controller = new Harness(authAction, parser)
          val result = controller.onPageLoad(fakeRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
        }
      }

      "the user has an unsupported affinity group" must {
        "redirect the user to the unauthorised page" in {
          val authAction = new CacheIdentifierActionImpl(new FakeFailingAuthConnector(new UnsupportedAffinityGroup), frontendAppConfig, mockJourneyService, parser)
          val controller = new Harness(authAction, parser)
          val result = controller.onPageLoad(fakeRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
        }
      }

      "the user has an unsupported credential role" must {
        "redirect the user to the unauthorised page" in {
          val authAction = new CacheIdentifierActionImpl(new FakeFailingAuthConnector(new UnsupportedCredentialRole), frontendAppConfig, mockJourneyService, parser)
          val controller = new Harness(authAction, parser)
          val result = controller.onPageLoad(fakeRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
        }
      }
    }
  }

}

class FakeFailingAuthConnector(exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
    Future.failed(exceptionToReturn)
}
