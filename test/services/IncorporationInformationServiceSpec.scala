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
import models.{Name, Officer}
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

  val tstFilteredOfficer = Seq(
    Officer(Name(Some("first"), Some("middle"), "last", Some("Mr")), "director", None, None)
  )

  val tstOfficerListJson = Json.parse(
    """
      |{
      |  "officers": [
      |    {
      |      "name_elements" : {
      |        "forename" : "test1",
      |        "other_forenames" : "test11",
      |        "surname" : "testa",
      |        "title" : "Mr"
      |      },
      |      "officer_role" : "cic-manager"
      |    }, {
      |      "name" : "test",
      |      "name_elements" : {
      |        "forename" : "test2",
      |        "other_forenames" : "test22",
      |        "surname" : "testb",
      |        "title" : "Mr"
      |      },
      |      "officer_role" : "corporate-director"
      |    }, {
      |      "name" : "test",
      |      "name_elements" : {
      |        "forename" : "first",
      |        "other_forenames" : "middle",
      |        "surname" : "last",
      |        "title" : "Mr"
      |      },
      |      "officer_role" : "director"
      |    }
      |  ]
      |}""".stripMargin)

  val tstOfficerListNoDirectorJson = Json.parse(
    """
      |{
      |  "officers": [
      |    {
      |      "name_elements" : {
      |        "forename" : "test1",
      |        "other_forenames" : "test11",
      |        "surname" : "testa",
      |        "title" : "Mr"
      |      },
      |      "officer_role" : "cic-manager"
      |    }
      |  ]
      |}""".stripMargin)

  "retrieveIncorporationDate" should {
    "find an incorp date" when {
      "it exists in II" in new Setup {
        when(mockIIConnector.getIncorpData(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(Some(Json.obj("incorporationDate" -> testDate.get))))

        await(service.getIncorpDate("transID")) mustBe testDate
      }
    }

    "fail to find an incorp date" when {
      "it does not exist in II" in new Setup {
        when(mockIIConnector.getIncorpData(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(None))

        await(service.getIncorpDate("transID")) mustBe None
      }
    }
  }

  val officer = Officer(Name(Some("TestFirst"), None, "TestSurname", None), "Director", None, None)
  val officerNotDirector = Officer(Name(Some("TestFirst"), None, "TestSurname", None), "not a director", None, None)

  "retrieveOfficers" should {
    "find officers" when {
      "return an officer lists with only directors and secretaries" in new Setup {
        when(mockIIConnector.getOfficerList(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(tstOfficerListJson))

        await(service.getOfficerList("transID")) mustBe tstFilteredOfficer
      }
    }

    "fail to find officers" when {
      "they are not available in II" in new Setup {
        when(mockIIConnector.getOfficerList(Matchers.any())(Matchers.any()))
          .thenReturn(Future.failed(new RuntimeException("some exception")))

        intercept[RuntimeException](await(service.getOfficerList("transID")))
      }
      "officer list contains no directors or secretaries" in new Setup {
        when(mockIIConnector.getOfficerList(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(tstOfficerListNoDirectorJson))

        intercept[RuntimeException](await(service.getOfficerList("transID")))
      }
    }
  }
}
