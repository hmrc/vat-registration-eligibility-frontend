/*
 * Copyright 2024 HM Revenue & Customs
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

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.routes
import models.requests.CacheIdentifierRequest
import play.api.mvc.Results._
import play.api.mvc._
import services.JourneyService
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException, UnauthorizedException}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}


class CacheIdentifierActionImpl @Inject()(override val authConnector: AuthConnector,
                                          config: FrontendAppConfig,
                                          val journeyService: JourneyService,
                                          val parser: BodyParsers.Default
                                         )(implicit val executionContext: ExecutionContext)
  extends CacheIdentifierAction with AuthorisedFunctions {

  override def invokeBlock[A](request: Request[A], block: CacheIdentifierRequest[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised().retrieve(Retrievals.internalId) {
      _.map { internalId =>
          journeyService.getProfile.flatMap {
            case Some(profile) =>
              block(CacheIdentifierRequest(request, profile.registrationID, internalId))
            case _ =>
              Future.successful(Redirect(s"${config.vatRegFEURL}${config.vatRegFEURI}"))
          }
      }.getOrElse(throw new UnauthorizedException("Unable to retrieve internal Id"))
    } recover {
      case ex: NotFoundException =>
        Redirect(s"${config.vatRegFEURL}${config.vatRegFEURI}")
      case ex: NoActiveSession =>
        Redirect(config.loginUrl, Map("continue" -> Seq(config.postSignInUrl)))
      case ex: InsufficientEnrolments =>
        Redirect(routes.UnauthorisedController.onPageLoad)
      case ex: InsufficientConfidenceLevel =>
        Redirect(routes.UnauthorisedController.onPageLoad)
      case ex: UnsupportedAuthProvider =>
        Redirect(routes.UnauthorisedController.onPageLoad)
      case ex: UnsupportedAffinityGroup =>
        Redirect(routes.UnauthorisedController.onPageLoad)
      case ex: UnsupportedCredentialRole =>
        Redirect(routes.UnauthorisedController.onPageLoad)
    }
  }
}

trait CacheIdentifierAction
  extends ActionBuilder[CacheIdentifierRequest, AnyContent]
    with ActionFunction[Request, CacheIdentifierRequest]
