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

import connectors.VatRegistrationConnector
import models.CurrentProfile
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyService @Inject()(val sessionService: SessionService,
                               val vatRegistrationConnector: VatRegistrationConnector,
                               s4LService: S4LService)
                              (implicit ec: ExecutionContext) {

  private val profileKey = "CurrentProfile"
  private val dataKey = "eligibility-data"

  def emptyCacheMap(regId: String)(implicit hc: HeaderCarrier): CacheMap =
    CacheMap(id = sessionService.sessionId, data = Map(profileKey -> Json.toJson(CurrentProfile(regId))))

  def getProfile(implicit hc: HeaderCarrier): Future[Option[CurrentProfile]] =
    sessionService.getEntry[CurrentProfile](profileKey)

  def initialiseJourney(internalId: String, regId: String)(implicit hc: HeaderCarrier): Future[CacheMap] =
    sessionService.fetch(internalId)
      .flatMap {
        case Some(cacheMap) =>
          Future.successful(cacheMap)
        case _ =>
          s4LService.fetchAndGet[CacheMap](regId, dataKey)
            .map(_.getOrElse(emptyCacheMap(regId)))
      }
      .flatMap(cacheMap => sessionService.save(cacheMap.copy(id = sessionService.sessionId)))

}
