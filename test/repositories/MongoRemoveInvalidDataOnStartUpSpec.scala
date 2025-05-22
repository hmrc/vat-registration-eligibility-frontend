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
import featureswitch.core.models.FeatureSwitch.{DeleteAllInvalidTimestampData, DeleteSomeInvalidTimestampData}
import org.apache.pekko.actor.ActorSystem
import org.mockito.ArgumentMatchers.anyInt
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

  abstract class TestMongoRemoveInvalidDataOnStartUp extends MongoRemoveInvalidDataOnStartUp(testActorSystem, mockSessionRepository) {
    override def jitterDelay: FiniteDuration = FiniteDuration(0, TimeUnit.SECONDS)

    def deleteAllDataConfig(): String
    def deleteSomeDataConfig(): String

    when(mockServicesConfig.getString(DeleteAllInvalidTimestampData.configName)).thenReturn(deleteAllDataConfig())
    when(mockServicesConfig.getString(DeleteSomeInvalidTimestampData.configName)).thenReturn(deleteSomeDataConfig())
  }

  private val deleteLimitKey = "limit-for-deleting-invalid-timestamp-data"
  when(mockAppConfig.servicesConfig).thenReturn(mockServicesConfig)
  when(mockAppConfig.deleteLimitDatabaseConfigKey).thenReturn(deleteLimitKey)

  "MongoRemoveInvalidDataOnStartUp" should {
//    "not start deletion process" when {
//      "'DeleteAllInvalidTimestampData' and 'DeleteSomeInvalidTimestampData' switches are disabled" in {
//        when(mockServicesConfig.getString(DeleteAllInvalidTimestampData.configName)).thenReturn("false")
//        when(mockServicesConfig.getString(DeleteSomeInvalidTimestampData.configName)).thenReturn("false")
//
//        new TestMongoRemoveInvalidDataOnStartUp()
//
//        Thread.sleep(100)
//
//        verify(mockSessionRepository, never()).deleteAllDataWithLastUpdatedStringType()
//        verify(mockSessionRepository, never()).deleteNDataWithLastUpdatedStringType(anyInt())
//      }
//      "both 'DeleteAllInvalidTimestampData' and 'DeleteSomeInvalidTimestampData' switches are enabled, causing conflict" in {
//        when(mockServicesConfig.getString(DeleteAllInvalidTimestampData.configName)).thenReturn("true")
//        when(mockServicesConfig.getString(DeleteSomeInvalidTimestampData.configName)).thenReturn("true")
//
//        new TestMongoRemoveInvalidDataOnStartUp()
//
//        Thread.sleep(100)
//
//        verify(mockSessionRepository, never()).deleteAllDataWithLastUpdatedStringType()
//        verify(mockSessionRepository, never()).deleteNDataWithLastUpdatedStringType(anyInt())
//      }
//      "'DeleteSomeInvalidTimestampData' switch is enabled but there is no configured deletion limit" in {
//        when(mockServicesConfig.getString(DeleteSomeInvalidTimestampData.configName)).thenReturn("true")
//        when(mockServicesConfig.getString(DeleteAllInvalidTimestampData.configName)).thenReturn("false")
//
//        new TestMongoRemoveInvalidDataOnStartUp()
//
//        Thread.sleep(100)
//
//        verify(mockSessionRepository, never()).deleteNDataWithLastUpdatedStringType(anyInt())
//        verify(mockSessionRepository, never()).deleteAllDataWithLastUpdatedStringType()
//      }
//      "'DeleteSomeInvalidTimestampData' switch is enabled but the configured deletion limit is not a valid Int" in {
//        when(mockServicesConfig.getString(DeleteSomeInvalidTimestampData.configName)).thenReturn("true")
//        when(mockServicesConfig.getString(DeleteAllInvalidTimestampData.configName)).thenReturn("false")
//        when(mockServicesConfig.getString(deleteLimitKey)).thenReturn("invalidInt")
//
//        new TestMongoRemoveInvalidDataOnStartUp()
//
//        Thread.sleep(100)
//
//        verify(mockSessionRepository, never()).deleteNDataWithLastUpdatedStringType(anyInt())
//        verify(mockSessionRepository, never()).deleteAllDataWithLastUpdatedStringType()
//      }
//      "'DeleteSomeInvalidTimestampData' switch is enabled but the configured deletion limit is not a positive Int" in {
//        when(mockServicesConfig.getString(DeleteSomeInvalidTimestampData.configName)).thenReturn("true")
//        when(mockServicesConfig.getString(DeleteAllInvalidTimestampData.configName)).thenReturn("false")
//        when(mockServicesConfig.getString(deleteLimitKey)).thenReturn("0")
//
//        new TestMongoRemoveInvalidDataOnStartUp()
//
//        Thread.sleep(100)
//
//        verify(mockSessionRepository, never()).deleteNDataWithLastUpdatedStringType(anyInt())
//        verify(mockSessionRepository, never()).deleteAllDataWithLastUpdatedStringType()
//      }
//    }

    "make a call to the database to delete all the invalid data" when {
      "'DeleteAllInvalidTimestampData' switch is enabled (and 'DeleteSomeInvalidTimestampData' is disabled)" in new TestMongoRemoveInvalidDataOnStartUp {
        override def deleteAllDataConfig() = "true"
        override def deleteSomeDataConfig() = "false"

        when(mockSessionRepository.deleteAllDataWithLastUpdatedStringType()).thenReturn(Future.successful(DeleteResult.acknowledged(5)))

        Thread.sleep(100)

        verify(mockSessionRepository, times(1)).deleteAllDataWithLastUpdatedStringType()
        verify(mockSessionRepository, never()).deleteNDataWithLastUpdatedStringType(anyInt())
      }
    }

    "make a call to the database to delete the invalid data up to the config limit" when {
      "'DeleteSomeInvalidTimestampData' (and not 'DeleteAllInvalidTimestampData') switch is enabled with a configured deletion limit" in new TestMongoRemoveInvalidDataOnStartUp {
        val limit = 2
        override def deleteAllDataConfig() = "true"
        override def deleteSomeDataConfig() = "false"
        when(mockServicesConfig.getString(deleteLimitKey)).thenReturn(limit.toString)
        when(mockSessionRepository.deleteNDataWithLastUpdatedStringType(limit)).thenReturn(Future.successful(DeleteResult.acknowledged(limit)))

        Thread.sleep(100)

        verify(mockSessionRepository, times(1)).deleteNDataWithLastUpdatedStringType(limit)
        verify(mockSessionRepository, never()).deleteAllDataWithLastUpdatedStringType()
      }
    }
  }
}
