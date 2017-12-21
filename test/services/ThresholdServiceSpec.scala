/*
 * Copyright 2017 HM Revenue & Customs
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

import common.enums.VatRegStatus
import fixtures.{S4LFixture, VatRegistrationFixture}
import helpers.FutureAssertions
import mocks.VatMocks
import models.CurrentProfile
import models.view.{ExpectationOverThresholdView, OverThresholdView, TaxableTurnover, Threshold, VoluntaryRegistration, VoluntaryRegistrationReason}
import models.view.VoluntaryRegistration._
import models.view.VoluntaryRegistrationReason._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class ThresholdServiceSpec extends UnitSpec with MockitoSugar with VatMocks with FutureAssertions with VatRegistrationFixture with S4LFixture {
  implicit val hc = HeaderCarrier()
  implicit val currentProfilePreIncorp = CurrentProfile("Test Me", testRegId, "000-434-1", VatRegStatus.draft, None)
  val currentProfilePostIncorp = CurrentProfile("Test Me", testRegId, "000-434-1", VatRegStatus.draft, Some(LocalDate.of(2016, 12, 21)))

  val emptyThreshold = Threshold(None, None, None, None, None)
  val taxableTurnoverNO = TaxableTurnover(TaxableTurnover.TAXABLE_NO)
  val taxableTurnoverYES = TaxableTurnover(TaxableTurnover.TAXABLE_YES)
  val voluntaryRegistrationYES = VoluntaryRegistration(REGISTER_YES)
  val voluntaryRegistrationNO = VoluntaryRegistration(REGISTER_NO)
  val voluntaryRegistrationReasonSELLS = VoluntaryRegistrationReason(SELLS)
  val thresholdPreIncorpComplete = Threshold(Some(taxableTurnoverNO), Some(voluntaryRegistrationYES), Some(voluntaryRegistrationReasonSELLS), None, None)
  val overThresholdFalse = OverThresholdView(false, None)
  val overThresholdTrue = OverThresholdView(true, testDate)
  val expectOverThresholdFalse = ExpectationOverThresholdView(false, None)
  val expectOverThresholdTrue = ExpectationOverThresholdView(true, testDate)
  val thresholdPostIncorpComplete = Threshold(None, Some(voluntaryRegistrationYES), Some(voluntaryRegistrationReasonSELLS), Some(overThresholdFalse), Some(expectOverThresholdFalse))
  val thresholdPostIncorpCompleteOver1 = Threshold(None, Some(voluntaryRegistrationNO), None, Some(overThresholdTrue), Some(expectOverThresholdFalse))
  val thresholdPostIncorpCompleteOver2 = Threshold(None, Some(voluntaryRegistrationNO), None, Some(overThresholdFalse), Some(expectOverThresholdTrue))

  class Setup(s4lData: Option[Threshold] = None, backendData: Option[JsValue] = None) {
    val service = new ThresholdService {
      override val s4LConnector = mockS4LConnector
      override val vatRegistrationConnector = mockRegConnector
    }

    when(mockS4LConnector.fetchAndGet[Threshold](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(s4lData))

    getThresholdMock(Future.successful(backendData))

    when(mockS4LConnector.save(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))
  }

  class SetupForS4L(t: Threshold = emptyThreshold) {
    val service = new ThresholdService {
      override val vatRegistrationConnector = mockRegConnector
      override val s4LConnector = mockS4LConnector

      override def getThreshold(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[Threshold] = {
        Future.successful(t)
      }
    }

    when(mockS4LConnector.save(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))
  }

  class SetupForSaveBackend(t: Threshold = validThresholdPreIncorp) {
    val service = new ThresholdService {
      override val vatRegistrationConnector = mockRegConnector
      override val s4LConnector = mockS4LConnector

      override def getThreshold(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[Threshold] = {
        Future.successful(t)
      }
    }
    patchThresholdMock(Future.successful(Json.toJson("""{}""")))


    when(mockS4LConnector.clear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(HttpResponse(200)))
  }

  "Calling getThreshold" should {
    val partialThreshold = Threshold(Some(taxableTurnoverNO), Some(voluntaryRegistrationYES), None, None, None)
    val jsonMandatoryNO = Json.parse(
      s"""
         |{
         |  "mandatoryRegistration": false,
         |  "voluntaryReason": "$SELLS"
         |}
       """.stripMargin)
    val jsonPreIncorpMandatoryYES = Json.parse(
      s"""
         |{
         |  "mandatoryRegistration": true
         |}
       """.stripMargin)
    val jsonPostIncorpMandatoryYES1 = Json.parse(
      s"""
         |{
         |  "mandatoryRegistration": true,
         |  "overThresholdDate": "2017-03-21"
         |}
       """.stripMargin)
    val jsonPostIncorpMandatoryYES2 = Json.parse(
      s"""
         |{
         |  "mandatoryRegistration": true,
         |  "expectedOverThresholdDate": "2017-03-21"
         |}
       """.stripMargin)

    "return a default Threshold view model if nothing is in S4L & backend" in new Setup {
      service.getThreshold returns emptyThreshold
    }

    "return a partial Threshold view model from S4L" in new Setup(Some(partialThreshold)) {
      service.getThreshold returns partialThreshold
    }

    "return a complete pre incorp Threshold view model with Taxable Turnover set to NO from backend" in new Setup(None, Some(jsonMandatoryNO)) {
      service.getThreshold returns thresholdPreIncorpComplete
    }

    "return a complete pre incorp Threshold view model with Taxable Turnover set to YES from backend" in new Setup(None, Some(jsonPreIncorpMandatoryYES)) {
      val expected = Threshold(Some(taxableTurnoverYES), Some(voluntaryRegistrationNO), None, None, None)

      service.getThreshold returns expected
    }

    "return a complete post incorp Threshold view model with both OverThreshold set to false from backend" in new Setup(None, Some(jsonMandatoryNO)) {
      service.getThreshold(currentProfilePostIncorp, hc) returns thresholdPostIncorpComplete
    }

    "return a complete post incorp Threshold view model with OverThreshold set to true from backend" in new Setup(None, Some(jsonPostIncorpMandatoryYES1)) {
      service.getThreshold(currentProfilePostIncorp, hc) returns thresholdPostIncorpCompleteOver1
    }

    "return a complete post incorp Threshold view model with ExpectedOverThreshold set to true from backend" in new Setup(None, Some(jsonPostIncorpMandatoryYES2)) {
      service.getThreshold(currentProfilePostIncorp, hc) returns thresholdPostIncorpCompleteOver2
    }
  }

  "Calling saveThreshold" should {
    val voluntaryRegistrationReasonINTENDS = VoluntaryRegistrationReason(INTENDS_TO_SELL)
    val voluntaryRegistrationReasonNEITHER = VoluntaryRegistrationReason(NEITHER)

    val thresholdTaxableTurnoverNO = Threshold(Some(taxableTurnoverNO), None, None, None, None)
    val thresholdPreIncorpIncomplete = Threshold(Some(taxableTurnoverNO), Some(voluntaryRegistrationYES), None, None, None)
    val thresholdOverThresholdFalse = Threshold(None, None, None, Some(overThresholdFalse), Some(expectOverThresholdFalse))
    val thresholdPostIncorpIncomplete = Threshold(None, Some(voluntaryRegistrationYES), None, Some(overThresholdFalse), Some(expectOverThresholdFalse))

    "save to S4L and return an incomplete pre incorp Threshold with Taxable Turnover set to NO" in new SetupForS4L {
      await(service.saveThreshold(taxableTurnoverNO)) shouldBe thresholdTaxableTurnoverNO
    }

    "save to S4L and return an incomplete pre incorp Threshold with Voluntary Registration set to YES" in new SetupForS4L(thresholdTaxableTurnoverNO) {
      await(service.saveThreshold(voluntaryRegistrationYES)) shouldBe thresholdPreIncorpIncomplete
    }

    "save to S4L and return an incomplete post incorp Threshold with OverThreshold set to false" in new SetupForS4L {
      await(service.saveThreshold(overThresholdFalse)) shouldBe Threshold(None, None, None, Some(overThresholdFalse), None)
    }

    "save to S4L and return an incomplete post incorp Threshold with ExpectationOverThreshold set to false" in new SetupForS4L {
      await(service.saveThreshold(expectOverThresholdFalse)) shouldBe Threshold(None, None, None, None, Some(expectOverThresholdFalse))
    }

    "save to S4L and return an incomplete post incorp Threshold with Voluntary Registration set to YES" in new SetupForS4L(thresholdOverThresholdFalse) {
      await(service.saveThreshold(voluntaryRegistrationYES)) shouldBe thresholdPostIncorpIncomplete
    }

    "save to backend and return a complete pre incorp Threshold with Taxable Turnover set to YES" in new SetupForSaveBackend(emptyThreshold) {
      val expected = Threshold(Some(taxableTurnoverYES), Some(voluntaryRegistrationNO), None, None, None)
      await(service.saveThreshold(taxableTurnoverYES)) shouldBe expected
    }

    "save to backend and return a complete pre incorp Threshold with Taxable Turnover set to NO" in new SetupForSaveBackend(thresholdPreIncorpIncomplete) {
      await(service.saveThreshold(voluntaryRegistrationReasonSELLS)) shouldBe thresholdPreIncorpComplete
    }

    "save to backend and return a complete pre incorp Threshold with Voluntary Registration Reason set to INTENDS_TO_SELL" in new SetupForSaveBackend(thresholdPreIncorpIncomplete) {
      await(service.saveThreshold(voluntaryRegistrationReasonINTENDS)) shouldBe thresholdPreIncorpComplete.copy(voluntaryRegistrationReason = Some(voluntaryRegistrationReasonINTENDS))
    }

    "save to backend and return a complete pre incorp Threshold with Voluntary Registration Reason set to NEITHER" in new SetupForSaveBackend(thresholdPreIncorpIncomplete) {
      await(service.saveThreshold(voluntaryRegistrationReasonNEITHER)) shouldBe thresholdPreIncorpComplete.copy(voluntaryRegistrationReason = Some(voluntaryRegistrationReasonNEITHER))
    }

    "save to backend and return a complete post incorp Threshold with OverThreshold set to YES" in new SetupForSaveBackend(emptyThreshold.copy(expectationOverThreshold = Some(expectOverThresholdFalse))) {
      await(service.saveThreshold(overThresholdTrue)) shouldBe thresholdPostIncorpCompleteOver1
    }

    "save to backend and return a complete post incorp Threshold with ExpectedOverThreshold set to YES" in new SetupForSaveBackend(emptyThreshold.copy(overThreshold = Some(overThresholdFalse))) {
      await(service.saveThreshold(expectOverThresholdTrue)) shouldBe thresholdPostIncorpCompleteOver2
    }

    "save to backend and return a complete post incorp Threshold with Voluntary Registration set to YES" in new SetupForSaveBackend(thresholdPostIncorpIncomplete) {
      await(service.saveThreshold(voluntaryRegistrationReasonSELLS)) shouldBe thresholdPostIncorpComplete
    }
  }
}
