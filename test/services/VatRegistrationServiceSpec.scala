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

import base.{SpecBase, VATEligibilityMocks}
import identifiers._
import models._
import models.requests.DataRequest
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.libs.json._
import play.api.mvc.AnyContentAsEmpty
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.collection.immutable.ListMap
import scala.concurrent.Future

class VatRegistrationServiceSpec extends SpecBase with VATEligibilityMocks {

  class Setup {
    val service = new VatRegistrationService(
      mockVatRegConnector,
      mockSessionService,
      mockMessagesAPI
    )

    val mockMessages: Messages = mock[Messages]

    when(mockMessagesAPI.preferred(ArgumentMatchers.any[DataRequest[_]]()))
      .thenReturn(mockMessages)

    when(mockMessages.apply(ArgumentMatchers.anyString(), ArgumentMatchers.any()))
      .thenReturn("mocked message")
  }

  implicit val r: DataRequest[AnyContentAsEmpty.type] = fakeDataRequestIncorped
  val internalId = "internalID"

  "prepareQuestionData" should {
    "prepare simple boolean data" in new Setup {
      val key = "thresholdNextThirtyDays"

      service.prepareQuestionData(key, data = false) mustBe
        List(Json.obj(
          "questionId" -> key,
          "question" -> "mocked message",
          "answer" -> "mocked message",
          "answerValue" -> false
        ))
    }

    "prepare simple string data" in new Setup {
      val key = "completionCapacity"

      service.prepareQuestionData(key, "officer") mustBe
        List(Json.obj(
          "questionId" -> key,
          "question" -> "mocked message",
          "answer" -> "officer",
          "answerValue" -> "officer"
        ))
    }
  }

  "submitEligibility" should {
    val fullListMapHappyPathTwelveMonthsFalse: ListMap[String, JsValue] = ListMap[String, JsValue](
      "" -> JsString(""),
      s"$FixedEstablishmentId" -> JsBoolean(true),
      s"$BusinessEntityId" -> Json.toJson(UKCompany),
      s"$ThresholdInTwelveMonthsId" -> Json.obj("value" -> JsBoolean(false)),
      s"$ThresholdNextThirtyDaysId" -> Json.obj("value" -> JsBoolean(false)),
      s"$VoluntaryRegistrationId" -> JsBoolean(true),
      s"$VoluntaryInformationId" -> JsBoolean(true),
      s"$TurnoverEstimateId" -> Json.obj("amount" -> JsString("50000")),
      s"$InternationalActivitiesId" -> JsBoolean(false),
      s"$InvolvedInOtherBusinessId" -> JsBoolean(false),
      s"$AnnualAccountingSchemeId" -> JsBoolean(false),
      s"$VoluntaryRegistrationId" -> JsBoolean(true),
      s"$VATExemptionId" -> JsBoolean(false),
      s"$ZeroRatedSalesId" -> JsBoolean(true),
      s"$RegisteringBusinessId" -> Json.toJson(OwnBusiness),
      s"$RegistrationReasonId" -> Json.toJson(SellingGoodsAndServices),
      s"$NinoId" -> JsBoolean(true),
      s"$AgriculturalFlatRateSchemeId" -> JsBoolean(false),
      s"$RacehorsesId" -> JsBoolean(false)
    )
    "return the JsObject submitted to Vat registration" in new Setup {
      when(mockSessionService.fetch(any())).thenReturn(Future.successful(Some(new CacheMap("foo", fullListMapHappyPathTwelveMonthsFalse))))
      when(mockVatRegConnector.saveEligibility(any(), any())(any(), any())).thenReturn(Future.successful(Json.obj("wizz" -> "woo")))

      await(service.submitEligibility("foo")) mustBe Json.parse(
        """
          |{"sections":[
          |{"title":"VAT-taxable sales",
          | "data":[
          | {"questionId":"thresholdInTwelveMonths-value","question":"mocked message","answer":"mocked message","answerValue":false},
          | {"questionId":"thresholdNextThirtyDays-value","question":"mocked message","answer":"mocked message","answerValue":false},
          | {"questionId":"voluntaryRegistration","question":"mocked message","answer":"mocked message","answerValue":true},
          | {"questionId":"voluntaryInformation","question":"mocked message","answer":"mocked message","answerValue":true},
          | {"questionId":"turnoverEstimate-value","question":"mocked message","answer":"£50,000","answerValue":50000}]},
          | {"title":"Special situations",
          | "data":[{"questionId":"fixedEstablishment","question":"mocked message","answer":"mocked message","answerValue":true},
          | {"questionId":"businessEntity-value","question":"mocked message","answer":"mocked message","answerValue":"50"},
          | {"questionId":"agriculturalFlatRateScheme","question":"mocked message","answer":"mocked message","answerValue":false},
          | {"questionId":"internationalActivities","question":"mocked message","answer":"mocked message","answerValue":false},
          | {"questionId":"involvedInOtherBusiness","question":"mocked message","answer":"mocked message","answerValue":false},
          | {"questionId":"racehorses","question":"mocked message","answer":"mocked message","answerValue":false},
          | {"questionId":"annualAccountingScheme","question":"mocked message","answer":"mocked message","answerValue":false},
          | {"questionId":"registeringBusiness-value","question":"mocked message","answer":"mocked message","answerValue":"own"},
          | {"questionId":"registrationReason-value","question":"mocked message","answer":"mocked message","answerValue":"selling-goods-and-services"},
          | {"questionId":"nino","question":"mocked message","answer":"mocked message","answerValue":true},
          | {"questionId":"zeroRatedSales","question":"mocked message","answer":"mocked message","answerValue":true},
          | {"questionId":"vatExemption","question":"mocked message","answer":"mocked message","answerValue":false}
          |]}]}
          |""".stripMargin)
    }
  }
}