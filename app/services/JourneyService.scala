/*
 * Copyright 2023 HM Revenue & Customs
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
                               val vatRegistrationConnector: VatRegistrationConnector)
                              (implicit ec: ExecutionContext) {

  private val profileKey = "CurrentProfile"

  def emptyCacheMap(regId: String)(implicit hc: HeaderCarrier): CacheMap =
    CacheMap(id = sessionService.sessionId, data = Map(profileKey -> Json.toJson(CurrentProfile(regId))))

  def getProfile(implicit hc: HeaderCarrier): Future[Option[CurrentProfile]] =
    sessionService.getEntry[CurrentProfile](profileKey)

  def initialiseJourney(regId: String)(implicit hc: HeaderCarrier): Future[CacheMap] = {
    sessionService.fetch.flatMap {
      case Some(cacheMap) if cacheMap.data.get(profileKey).contains(Json.toJson(CurrentProfile(regId))) =>
        Future.successful(cacheMap)
      case _ =>
        vatRegistrationConnector.getEligibilityAnswers(regId).flatMap {
          case Some(answers) if answers.get(profileKey).contains(Json.toJson(CurrentProfile(regId))) =>
            sessionService.save(emptyCacheMap(regId).copy(data = answers))
          case _ =>
            sessionService.save(emptyCacheMap(regId))
        }
    }
  }

}
