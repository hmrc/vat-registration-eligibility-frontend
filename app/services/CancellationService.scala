/*
 * Copyright 2017 HM Revenue & Customs
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

package services

import javax.inject.Inject

import common.enums.CacheKeys
import connectors.{KeystoreConnector, S4LConnector}
import models.CurrentProfile
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

class CancellationServiceImpl @Inject()(val keystoreConnector: KeystoreConnector,
                                        val s4LConnector: S4LConnector,
                                        val currentProfileService: CurrentProfileService) extends CancellationService

trait CancellationService {
  val keystoreConnector: KeystoreConnector
  val s4LConnector: S4LConnector
  val currentProfileService: CurrentProfileService

  def deleteEligibilityData(regId: String)(implicit hc: HeaderCarrier): Future[Boolean] = {
    getCurrentProfile flatMap { implicit profile =>
      if (regId != profile.registrationId) {
        logger.warn(s"[VatRegistrationService] [deleteEligibilityData] - Requested document regId: $regId does not correspond to the CurrentProfile regId")
        Future.successful(false)
      } else {
        clearSessionData
      }} recover {
      case ex: Exception =>
        logger.error(s"[VatRegistrationService] [deleteEligibilityData] - Received an error when deleting Registration regId: $regId - error: ${ex.getMessage}")
        throw ex
    }
  }

  private def clearSessionData(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Boolean] = for {
    _ <- keystoreConnector.remove()
    _ <- s4LConnector.clear(profile.registrationId)
  } yield true

  private def getCurrentProfile(implicit hc: HeaderCarrier): Future[CurrentProfile] = {
    keystoreConnector.fetchAndGet[CurrentProfile](CacheKeys.CurrentProfile.toString) flatMap(
      _.fold(currentProfileService.buildCurrentProfile)(cp => Future.successful(cp))
    )
  }
}
