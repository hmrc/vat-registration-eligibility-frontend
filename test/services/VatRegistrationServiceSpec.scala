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
import connectors.mocks.MockSessionService
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
import utils.UserAnswers

import java.time.LocalDate
import scala.collection.immutable.ListMap
import scala.concurrent.Future

class VatRegistrationServiceSpec extends SpecBase with VATEligibilityMocks with MockSessionService {

  class Setup {
    val service = new VatRegistrationService(
      mockVatRegConnector,
      sessionServiceMock,
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

  "submitEligibility" must {
    "return the JsObject submitted to Vat registration" in new Setup {
      val fullListMapHappyPathTwelveMonthsFalse: ListMap[String, JsValue] = ListMap[String, JsValue](
        "" -> JsString(""),
        s"$FixedEstablishmentId" -> JsBoolean(true),
        s"$BusinessEntityId" -> Json.toJson(UKCompany),
        s"$ThresholdInTwelveMonthsId" -> Json.obj("value" -> JsBoolean(false)),
        s"$ThresholdNextThirtyDaysId" -> Json.obj("value" -> JsBoolean(false)),
        s"$VoluntaryRegistrationId" -> JsBoolean(true),
        s"$InternationalActivitiesId" -> JsBoolean(false),
        s"$InvolvedInOtherBusinessId" -> JsBoolean(false),
        s"$VoluntaryRegistrationId" -> JsBoolean(true),
        s"$RegisteringBusinessId" -> Json.toJson(OwnBusiness),
        s"$RegistrationReasonId" -> Json.toJson(SellingGoodsAndServices),
        s"$NinoId" -> JsBoolean(true),
        s"$AgriculturalFlatRateSchemeId" -> JsBoolean(false),
        s"$RacehorsesId" -> JsBoolean(false)
      )

      mockSessionFetch()(Future.successful(Some(new CacheMap("foo", fullListMapHappyPathTwelveMonthsFalse))))
      when(mockVatRegConnector.saveEligibility(any(), any())(any(), any())).thenReturn(Future.successful(Json.obj("wizz" -> "woo")))

      await(service.submitEligibility) mustBe Json.parse(
        """
          |{"sections":[
          |{"title":"Registration Reason",
          | "data":[
          | {"questionId":"fixedEstablishment","question":"eligibility.cya.fixedEstablishment","answer":"eligibility.site.yes","answerValue":true},
          | {"questionId":"businessEntity","question":"eligibility.cya.businessEntity","answer":"eligibility.businessEntity.limited-company","answerValue":"50"},
          | {"questionId":"agriculturalFlatRateScheme","question":"eligibility.cya.agriculturalFlatRateScheme","answer":"eligibility.site.no","answerValue":false},
          | {"questionId":"internationalActivities","question":"eligibility.cya.internationalActivities","answer":"eligibility.site.no","answerValue":false},
          | {"questionId":"involvedInOtherBusiness","question":"eligibility.cya.involvedInOtherBusiness","answer":"eligibility.site.no","answerValue":false},
          | {"questionId":"racehorses","question":"eligibility.cya.racehorses","answer":"eligibility.site.no","answerValue":false},
          | {"questionId":"registeringBusiness","question":"eligibility.cya.registeringBusiness","answer":"eligibility.registeringBusiness.radioOwn","answerValue":"own"},
          | {"questionId":"registrationReason","question":"eligibility.cya.registrationReason","answer":"eligibility.registrationReason.sellingGoods.radio","answerValue":"selling-goods-and-services"},
          | {"questionId":"nino","question":"eligibility.cya.nino","answer":"eligibility.site.yes","answerValue":true},
          | {"questionId":"thresholdInTwelveMonths","question":"eligibility.cya.thresholdInTwelveMonths","answer":"eligibility.site.no","answerValue":false},
          | {"questionId":"thresholdNextThirtyDays","question":"eligibility.cya.thresholdNextThirtyDays","answer":"eligibility.site.no","answerValue":false},
          | {"questionId":"voluntaryRegistration","question":"eligibility.cya.voluntaryRegistration","answer":"eligibility.site.yes","answerValue":true}
          |]}]}
          |""".stripMargin)
    }

    "return the JsObject submitted to Vat registration for togc" in new Setup {
      val testPreviousName = "testPreviousName"
      val testVrn = "testVrn"

      val togcColeData: ListMap[String, JsValue] = ListMap[String, JsValue](
        s"$FixedEstablishmentId" -> JsBoolean(true),
        s"$BusinessEntityId" -> Json.toJson(UKCompany),
        s"$InternationalActivitiesId" -> JsBoolean(false),
        s"$InvolvedInOtherBusinessId" -> JsBoolean(false),
        s"$RegisteringBusinessId" -> Json.toJson(OwnBusiness),
        s"$RegistrationReasonId" -> Json.toJson(TakingOverBusiness),
        s"$NinoId" -> JsBoolean(true),
        s"$AgriculturalFlatRateSchemeId" -> JsBoolean(false),
        s"$RacehorsesId" -> JsBoolean(false),
        s"$DateOfBusinessTransferId" -> Json.obj("date" -> LocalDate.now()),
        s"$PreviousBusinessNameId" -> JsString(testPreviousName),
        s"$VATNumberId" -> JsString(testVrn),
        s"$KeepOldVrnId" -> JsBoolean(true),
        s"$TermsAndConditionsId" -> JsBoolean(true)
      )

      implicit val r: DataRequest[AnyContentAsEmpty.type] = fakeDataRequestIncorped.copy(userAnswers = new UserAnswers(CacheMap("1", togcColeData)))

      mockSessionFetch()(Future.successful(Some(new CacheMap("foo", togcColeData))))
      when(mockVatRegConnector.saveEligibility(any(), any())(any(), any())).thenReturn(Future.successful(Json.obj("wizz" -> "woo")))

      await(service.submitEligibility) mustBe Json.parse(
        s"""
           |{
           |  "sections":[
           |    {
           |      "title":"Registration Reason",
           |      "data":[
           |        {"questionId":"fixedEstablishment","question":"eligibility.cya.fixedEstablishment","answer":"eligibility.site.yes","answerValue":true},
           |        {"questionId":"businessEntity","question":"eligibility.cya.businessEntity","answer":"eligibility.businessEntity.limited-company","answerValue":"50"},
           |        {"questionId":"agriculturalFlatRateScheme","question":"eligibility.cya.agriculturalFlatRateScheme","answer":"eligibility.site.no","answerValue":false},
           |        {"questionId":"internationalActivities","question":"eligibility.cya.internationalActivities","answer":"eligibility.site.no","answerValue":false},
           |        {"questionId":"involvedInOtherBusiness","question":"eligibility.cya.involvedInOtherBusiness","answer":"eligibility.site.no","answerValue":false},
           |        {"questionId":"racehorses","question":"eligibility.cya.racehorses","answer":"eligibility.site.no","answerValue":false},
           |        {"questionId":"registeringBusiness","question":"eligibility.cya.registeringBusiness","answer":"eligibility.registeringBusiness.radioOwn","answerValue":"own"},
           |        {"questionId":"registrationReason","question":"eligibility.cya.registrationReason","answer":"eligibility.registrationReason.takingOver.radio","answerValue":"taking-over-business"},
           |        {"questionId":"nino","question":"eligibility.cya.nino","answer":"eligibility.site.yes","answerValue":true},
           |        {"questionId":"dateOfBusinessTransfer","question":"eligibility.cya.dateOfBusinessTransfer.togc","answer":"${LocalDate.now().format(service.formatter)}","answerValue":"${LocalDate.now()}"},
           |        {"questionId":"previousBusinessName","question":"eligibility.cya.previousBusinessName.togc","answer":"$testPreviousName","answerValue":"$testPreviousName"},
           |        {"questionId":"vatNumber","question":"eligibility.cya.vatNumber.togc","answer":"$testVrn","answerValue":"$testVrn"},
           |        {"questionId":"keepOldVrn","question":"eligibility.cya.keepOldVrn.togc","answer":"eligibility.site.yes","answerValue":true},
           |        {"questionId":"termsAndConditions","question":"eligibility.cya.termsAndConditions.togc","answer":"eligibility.site.yes","answerValue":true}
           |      ]
           |    }
           |  ]
           |}
           |""".stripMargin)
    }
  }
}