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

package services

import connectors.{SessionService, VatRegistrationConnector}
import models.CurrentProfile
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CurrentProfileService @Inject()(val sessionService: SessionService,
                                      val vatRegistrationConnector: VatRegistrationConnector,
                                      s4LService: S4LService)
                                     (implicit ec: ExecutionContext) {

  def fetchOrBuildCurrentProfile(internalID: String)(implicit headerCarrier: HeaderCarrier): Future[CurrentProfile] = {
    sessionService.getEntry[CurrentProfile](internalID, "CurrentProfile") flatMap {
      case Some(currentProfile) => Future.successful(currentProfile)
      case None => for {
        regId <- vatRegistrationConnector.getRegistrationId()
        optCacheMap <- s4LService.fetchAndGet[CacheMap](regId, "eligibility-data")
        currentProfile = optCacheMap.fold(CurrentProfile(regId))(cacheMap =>
          cacheMap.getEntry[CurrentProfile]("CurrentProfile").getOrElse(CurrentProfile(regId)))
        _ <- optCacheMap match {
          case Some(cacheMap) => sessionService.save(cacheMap)
          case None => sessionService.save(internalID, "CurrentProfile", currentProfile)
        }
      } yield currentProfile
    }
  }
}

