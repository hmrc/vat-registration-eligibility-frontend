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

import java.time.LocalDate
import javax.inject.Inject

import cats.data.OptionT
import cats.instances.FutureInstances
import common.enums.CacheKeys.CurrentProfile
import common.enums.VatRegStatus
import connectors.{KeystoreConnector, S4LConnector, VatRegistrationConnector}
import models._
import models.external.IncorporationInfo
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

class VatRegistrationServiceImpl @Inject()(val s4lConnector: S4LConnector,
                                           val keystoreConnector: KeystoreConnector,
                                           val vatRegConnector: VatRegistrationConnector) extends VatRegistrationService

trait VatRegistrationService extends FutureInstances {
  val vatRegConnector: VatRegistrationConnector
  val keystoreConnector: KeystoreConnector

  def getIncorporationInfo(txId: String)(implicit headerCarrier: HeaderCarrier): Future[Option[IncorporationInfo]] = {
    vatRegConnector.getIncorporationInfo(txId)
  }

  def getIncorporationDate(implicit currentProfile: CurrentProfile, headerCarrier: HeaderCarrier): Future[Option[LocalDate]] = {
    currentProfile.incorporationDate match {
      case None => for {
        incorpDate <- OptionT(getIncorporationInfo(currentProfile.transactionId)).subflatMap(_.statusEvent.incorporationDate).value
        _          <- keystoreConnector.cache[CurrentProfile](CurrentProfile.toString, currentProfile.copy(incorporationDate = incorpDate))
      } yield incorpDate
      case o@_ => Future.successful(o)
    }
  }

  def getStatus(regId: String)(implicit hc: HeaderCarrier): Future[VatRegStatus.Value] = {
    vatRegConnector.getStatus(regId)
  }
}
