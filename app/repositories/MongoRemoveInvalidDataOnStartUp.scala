/*
 * Copyright 2025 HM Revenue & Customs
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

package repositories

import config.FrontendAppConfig
import featureswitch.core.models.FeatureSwitch.DeleteInvalidTimestampData
import org.apache.pekko.actor.ActorSystem
import utils.LoggingUtil
import utils.PageIdBinding.isEnabled

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{DurationInt, FiniteDuration}

@Singleton
class MongoRemoveInvalidDataOnStartUp @Inject() (actorSystem: ActorSystem, sessionRepository: SessionRepository)(implicit
    appConfig: FrontendAppConfig,
    ec: ExecutionContext)
    extends LoggingUtil {

  protected def jitterDelay: FiniteDuration = (10 + scala.util.Random.nextInt(5)).seconds

  actorSystem.scheduler.scheduleOnce(jitterDelay) {
    logger.warn(s"[MongoRemoveInvalidDataOnStartUp] Start up job has started after delay of $jitterDelay.")
    countOrDeleteInvalidData()
    logger.warn(s"[MongoRemoveInvalidDataOnStartUp] Start up job has ended.")
  }

  private def countOrDeleteInvalidData(): Unit =
    if (isEnabled(DeleteInvalidTimestampData)) {
      logger.warn(
        s"[MongoRemoveInvalidDataOnStartUp] 'DeleteInvalidTimestampData' switch is set to true - starting deleteDataWithLastUpdatedString process.")
      sessionRepository.deleteDataWithLastUpdatedStringType()
    } else {
      logger.warn(s"[MongoRemoveInvalidDataOnStartUp] 'DeleteInvalidTimestampData' switch is set to false - no action taken.")
    }

}
