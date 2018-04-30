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

import javax.inject.Inject

import common.enums.CacheKeys.{CurrentProfile => CurrentProfileKey, _}
import common.enums.VatRegStatus
import connectors.{BusinessRegistrationConnector, CompanyRegistrationConnector, KeystoreConnector}
import models.CurrentProfile
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

class CurrentProfileServiceImpl @Inject()(val keystoreConnector: KeystoreConnector,
                                      val businessRegistrationConnector: BusinessRegistrationConnector,
                                      val compRegConnector: CompanyRegistrationConnector,
                                      val incorpInfoService: IncorporationInformationService,
                                      val vatRegistrationService: VatRegistrationService) extends CurrentProfileService

trait CurrentProfileService {
  val keystoreConnector: KeystoreConnector
  val businessRegistrationConnector: BusinessRegistrationConnector
  val compRegConnector: CompanyRegistrationConnector
  val incorpInfoService: IncorporationInformationService
  val vatRegistrationService: VatRegistrationService

  def getCurrentProfile()(implicit hc: HeaderCarrier): Future[CurrentProfile] = {
    keystoreConnector.fetchAndGet[CurrentProfile](CurrentProfileKey) flatMap { profile =>
      profile.fold(buildCurrentProfile)(profile => Future.successful(profile))
    }
  }

  private[services] def getRegIdAndStatus(implicit hc: HeaderCarrier): Future[(String, VatRegStatus.Value)] = {
    for {
      profile <- businessRegistrationConnector.retrieveBusinessProfile
      status  <- vatRegistrationService.getStatus(profile.registrationID)
    } yield {
      (profile.registrationID, status)
    }
  }

  private[services] def buildCurrentProfile(implicit hc: HeaderCarrier): Future[CurrentProfile] = {
    for {
      (regId, status)       <- getRegIdAndStatus
      companyProfile        <- compRegConnector.getCompanyRegistrationDetails(regId)
      companyName           <- incorpInfoService.getCompanyName(regId, companyProfile.transactionId)
      incorpInfo            <- vatRegistrationService.getIncorporationInfo(regId, companyProfile.transactionId)
      incorpDate            =  if(incorpInfo.isDefined) incorpInfo.get.statusEvent.incorporationDate else None
      profile               =  CurrentProfile(
        companyName           = companyName,
        registrationId        = regId,
        transactionId         = companyProfile.transactionId,
        vatRegistrationStatus = status,
        incorporationDate     = incorpDate
      )
      _                     <- keystoreConnector.cache[CurrentProfile](CurrentProfileKey.toString, profile)
    } yield profile
  }
}