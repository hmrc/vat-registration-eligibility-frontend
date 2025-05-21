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

package repositories

import com.mongodb.client.result.DeleteResult
import com.typesafe.config.ConfigFactory
import config.FrontendAppConfig
import featureswitch.core.models.FeatureSwitch.DeleteInvalidTimestampData
import org.apache.pekko.actor.ActorSystem
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class MongoRemoveInvalidDataOnStartUpSpec extends PlaySpec with MockitoSugar {

  private implicit val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  private val mockSessionRepository: SessionRepository  = mock[SessionRepository]
  private val mockServicesConfig: ServicesConfig        = mock[ServicesConfig]
  private val testActorSystem: ActorSystem              = ActorSystem("testActorSystem", ConfigFactory.load())

  class TestMongoRemoveInvalidDataOnStartUp extends MongoRemoveInvalidDataOnStartUp(testActorSystem, mockSessionRepository) {
    override def jitterDelay: FiniteDuration = FiniteDuration(0, TimeUnit.SECONDS)
  }

  when(mockAppConfig.servicesConfig).thenReturn(mockServicesConfig)

  "MongoRemoveInvalidDataOnStartUp" when {
    "DeleteInvalidTimestampData switch is disabled" should {
      "not start deletion process" in {
        when(mockServicesConfig.getString(DeleteInvalidTimestampData.configName)).thenReturn("false")

        new TestMongoRemoveInvalidDataOnStartUp()

        Thread.sleep(100)

        verify(mockSessionRepository, never()).deleteDataWithLastUpdatedStringType()
      }
    }

    "DeleteInvalidTimestampData switch is enabled" should {
      "make a call to the database to delete the invalid data" in {
        when(mockServicesConfig.getString(DeleteInvalidTimestampData.configName)).thenReturn("true")
        when(mockSessionRepository.deleteDataWithLastUpdatedStringType()).thenReturn(Future.successful(DeleteResult.acknowledged(5)))

        new TestMongoRemoveInvalidDataOnStartUp()

        Thread.sleep(100)

        verify(mockSessionRepository, times(1)).deleteDataWithLastUpdatedStringType()
      }
    }
  }
}
