/*
 * Copyright 2018 HM Revenue & Customs
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

import connectors.DataCacheConnector
import javax.inject.Inject
import models.CurrentProfile
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

class CurrentProfileServiceImpl @Inject()(
                                           val dataCacheConnector: DataCacheConnector,
                                           val incorporationInformationService: IncorporationInformationService) extends CurrentProfileService {

}

trait CurrentProfileService {
  val dataCacheConnector: DataCacheConnector
  val incorporationInformationService: IncorporationInformationService
//  val companyRegService: _ = ???
//  val businessRegService: _ = ???

  private def constructCurrentProfile(internalID : String)(implicit headerCarrier: HeaderCarrier) = for {
    regID           <- Future.successful("registrationID")
    transId         <- Future.successful("transactionID")
    incorpDate      <- incorporationInformationService.getIncorpDate(transId)
    currentProfile  = CurrentProfile(regID, transId, incorpDate)
    _               <- dataCacheConnector.save(internalID, "CurrentProfile", currentProfile)
  } yield currentProfile

  def fetchOrBuildCurrentProfile(internalID : String)(implicit headerCarrier: HeaderCarrier): Future[CurrentProfile] = {
    dataCacheConnector.getEntry[CurrentProfile](internalID, "CurrentProfile") flatMap {
      case Some(currentProfile) => Future.successful(currentProfile)
      case _                    => constructCurrentProfile(internalID)
    }
  }
}
