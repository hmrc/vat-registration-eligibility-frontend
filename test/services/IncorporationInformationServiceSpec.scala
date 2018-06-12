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

import java.time.LocalDate

import base.{CommonSpecBase, VATEligiblityMocks}
import connectors.{DataCacheConnector, IncorporationInformationConnector}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class IncorporationInformationServiceSpec extends CommonSpecBase with VATEligiblityMocks {

  class Setup {
    val service = new IncorporationInformationService {
      override val iiConnector: IncorporationInformationConnector = mockIIConnector
      override val dataCacheConnector: DataCacheConnector = mockDataCacheConnector
    }
  }

  val testDate = Some(LocalDate.of(2010, 10, 10))

  "retrieveIncorporationDate" should {
    "find an incorp date" when {
      "it exists in II and was not cached" in new Setup {
        when(mockIIConnector.getIncorpData(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(Some(Json.obj("incorporationDate" -> testDate.get))))

        when(mockDataCacheConnector.getEntry(Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(None))

        when(mockDataCacheConnector.save(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(CacheMap("id", Map("test" -> Json.obj()))))

        await(service.retrieveIncorporationDate("transID")) mustBe testDate
      }

      "it is cached" in new Setup {
        when(mockDataCacheConnector.getEntry[LocalDate](Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(testDate))

        await(service.retrieveIncorporationDate("transID")) mustBe testDate
      }
    }

    "fail to find an incorp date" when {
      "it does not exist in II nor was it saved in cache" in new Setup {
        when(mockDataCacheConnector.getEntry(Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(None))

        when(mockIIConnector.getIncorpData(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(None))

        await(service.retrieveIncorporationDate("transID")) mustBe None
      }
    }
  }

  "saveIncorpDateToDataCache" should {
    "save an incorp date to the data cache" when {
      "it is provided one" in new Setup {
        when(mockDataCacheConnector.save(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(CacheMap("id", Map("test" -> Json.obj()))))

        await(service.saveIncorpDateToDataCache(testDate)) mustBe testDate
      }
    }

    "not save" when {
      "it is not provided one" in new Setup {
        await(service.saveIncorpDateToDataCache(None)) mustBe None
      }
    }
  }
}
