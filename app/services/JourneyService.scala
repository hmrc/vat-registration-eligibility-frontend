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

package services

import connectors.VatRegistrationConnector
import models.CurrentProfile
import play.api.libs.json.Json
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.LinkLogger.infoLog

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

  def initialiseJourney(regId: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[CacheMap] = {
    sessionService.fetch.flatMap {
      case Some(cacheMap) if cacheMap.data.get(profileKey).contains(Json.toJson(CurrentProfile(regId))) =>
        infoLog(s"[JourneyService][initialiseJourney] Fetched cache for regId $regId")
        Future.successful(cacheMap)
      case _ =>
        vatRegistrationConnector.getEligibilityAnswers(regId).flatMap {
          case Some(answers) if answers.get(profileKey).contains(Json.toJson(CurrentProfile(regId))) =>
            infoLog(s"[JourneyService][initialiseJourney] Fetched eligibility answers from backend for regId $regId")
            sessionService.save(emptyCacheMap(regId).copy(data = answers))
          case _ =>
            infoLog(s"[JourneyService][initialiseJourney] Saving empty cache for regId $regId")
            sessionService.save(emptyCacheMap(regId))

        }
    }
  }

}
