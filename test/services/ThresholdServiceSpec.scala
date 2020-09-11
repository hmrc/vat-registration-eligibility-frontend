/*
 * Copyright 2020 HM Revenue & Customs
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

import base.{SpecBase, VATEligiblityMocks}
import config.FrontendAppConfig
import connectors.DataCacheConnector
import deprecated.DeprecatedConstants._
import identifiers.VoluntaryRegistrationId
import models.CurrentProfile
import models.requests.DataRequest
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsBoolean, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.UserAnswers

import scala.concurrent.Future

class ThresholdServiceSpec extends SpecBase with VATEligiblityMocks {

  val mockCache = CacheMap("testInternalId", Map(VoluntaryRegistrationId.toString -> JsBoolean(true)))
  implicit val msgs = messages

  class Setup(idate: Option[LocalDate] = Some(LocalDate.of(2018, 10, 4))) {
    def dr(incorpDate: Option[LocalDate]) = DataRequest(FakeRequest(), "testInternalId", CurrentProfile("testRegId"), new UserAnswers(mockCache))

    implicit val request: DataRequest[AnyContentAsEmpty.type] = dr(idate)
    val service = new ThresholdService {
      override val dataCacheConnector: DataCacheConnector = mockDataCacheConnector
      override val appConfig: FrontendAppConfig = frontendAppConfig
      override val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    }

  }

  "removeVoluntaryAndNextThirtyDays" should {
    "Future Successful CacheMap when DataCacheConnector removes both Voluntary and ThresholdNextThirtyDaysId" in new Setup {
      when(mockDataCacheConnector.removeEntry(any(), any())) thenReturn Future.successful(CacheMap("1", Map("bar" -> Json.obj("" -> ""))))
      await(service.removeVoluntaryAndNextThirtyDays) mustBe CacheMap("1", Map("bar" -> Json.obj("" -> "")))
      verify(mockDataCacheConnector, times(2)).removeEntry(any(), any())
    }
    "throw an exception when datacacheconnector returns an exception" in new Setup {
      when(mockDataCacheConnector.removeEntry(any(), any())) thenReturn Future.failed(new Exception())
      intercept[Exception](await(service.removeVoluntaryAndNextThirtyDays))
      verify(mockDataCacheConnector, times(1)).removeEntry(any(), any())
    }
  }

  "removeVoluntaryRegistration" should {
    "call dataCacheConnector and remove VoluntaryRegistrationId when" in new Setup {
      when(mockDataCacheConnector.removeEntry(any(), any())) thenReturn Future.successful(CacheMap("1", Map("foo" -> Json.obj("" -> ""))))
      await(service.removeVoluntaryRegistration) mustBe CacheMap("1", Map("foo" -> Json.obj("" -> "")))
      verify(mockDataCacheConnector, times(1)).removeEntry(any(), any())
    }
    "throw exception if dataCache throws an exception" in new Setup {
      when(mockDataCacheConnector.removeEntry(any(), any())) thenReturn Future.failed(new Exception("foo bar"))
      intercept[Exception](await(service.removeVoluntaryRegistration))
      verify(mockDataCacheConnector, times(1)).removeEntry(any(), any())
    }
  }
  "removeException" should {
    "call dataCacheConnector and remove VATRegistrationException" in new Setup {
      when(mockDataCacheConnector.removeEntry(any(), any())) thenReturn Future.successful(CacheMap("1", Map("foo" -> Json.obj("" -> ""))))
      await(service.removeException) mustBe CacheMap("1", Map("foo" -> Json.obj("" -> "")))
      verify(mockDataCacheConnector, times(1)).removeEntry(any(), any())
    }
    "throw exception if dataCache throws an exception" in new Setup {
      when(mockDataCacheConnector.removeEntry(any(), any())) thenReturn Future.failed(new Exception("foo bar"))
      intercept[Exception](await(service.removeException))
      verify(mockDataCacheConnector, times(1)).removeEntry(any(), any())
    }
  }
  //TODO - Fix these tests when dates for thresholds are determined
  "returnThresholdDateResult" ignore {
    "return less than 12 months heading text when incorpDate is less than 12 months" in new Setup(Some(LocalDate.now())) {
      service.returnThresholdDateResult(service.returnHeadingTwelveMonths) mustBe s"Since $fakeIncorpDateMessage, has Test Company made more than £85,000 in VAT-taxable sales?"
    }

    "return more than or equal to 12 months heading text when incorpDate is more than 12 months ago" in new Setup(Some(LocalDate.now().minusMonths(13))) {
      service.returnThresholdDateResult(service.returnHeadingTwelveMonths) mustBe s"In any 12-month period has $fakeCompanyName gone over the VAT-registration threshold?"
    }

    "return more than or equal to 12 months heading text when incorpDate is exactly 12 months ago" in new Setup(Some(LocalDate.now().minusMonths(12))) {
      service.returnThresholdDateResult(service.returnHeadingTwelveMonths) mustBe s"In any 12-month period has $fakeCompanyName gone over the VAT-registration threshold?"
    }

    "always return heading2 text when any incorpDate is used" in new Setup(Some(LocalDate.now())) {
      service.returnThresholdDateResult(service.returnHeadingForTwelveMonthsDateEntry) mustBe s"What month did $fakeCompanyName first go over the threshold?"
    }

    "always return not incorped helptext1 text when no incorpDate is used" in new Setup(None) {
      service.returnThresholdDateResult(service.returnHelpText1TwelveMonths).contains(
        "£85,000 is the current VAT-registration threshold. It is the amount of VAT-taxable sales sole traders can make before they have to register for VAT.") mustBe true
    }

    "always return less than 12 months helptext1 text when incorp date is less than 12 months ago" in new Setup(Some(LocalDate.now())) {
      service.returnThresholdDateResult(service.returnHelpText1TwelveMonths).contains(
        s"$fakeIncorpDateMessage is the date $fakeCompanyName was set up.") mustBe true
    }

    "always return less than 12 months helptext1 text when incorp date is 1 day under 12 months ago" in new Setup(Some(LocalDate.now().minusMonths(12).plusDays(1))) {
      service.returnThresholdDateResult(service.returnHelpText1TwelveMonths).contains(
        s"$fakeIncorpDateMessage is the date $fakeCompanyName was set up.") mustBe true
    }

    "always return morethan or equal 12 months helptext1 text when incorp date is exactly 12 months ago" in new Setup(Some(LocalDate.now().minusMonths(12))) {
      service.returnThresholdDateResult(service.returnHelpText1TwelveMonths).contains(
        s"£85,000 is the current VAT-registration threshold. It is the amount of VAT-taxable sales $fakeCompanyName can make before it has to register for VAT.") mustBe true
    }

    "always return morethan or equal 12 months helptext1 text when incorp date is over 12 months ago" in new Setup(Some(LocalDate.now().minusMonths(12).minusDays(1))) {
      service.returnThresholdDateResult(service.returnHelpText1TwelveMonths).contains(
        s"£85,000 is the current VAT-registration threshold. It is the amount of VAT-taxable sales $fakeCompanyName can make before it has to register for VAT.") mustBe true
    }
    "returnHelpText1Previous should return blank for limitedIncorpedEqualOrAfter20170401" in new Setup(Some(LocalDate.of(2017, 4, 2))) {
    }
    "returnHelpText1Previous should return limitedIncorpedTaxYear2016to2017 for companies incorped between 2016 and 2017" in new Setup(Some(LocalDate.of(2016, 4, 6))) {
      val res = service.returnThresholdDateResult(service.returnHelpText1Previous).body
      res.contains("VAT-registration thresholds can change. Recent ones include:") mustBe true
      res.contains("1 April 2017 to present: £85,000") mustBe true
      res.contains("1 April 2016 to 31 March 2017: £83,000") mustBe true
    }
    "returnHelpText1Previous should return limitedIncorpedTaxYear2015to2016 for companies incorped between 2015 and 2016" in new Setup(Some(LocalDate.of(2015, 4, 6))) {
      val res = service.returnThresholdDateResult(service.returnHelpText1Previous).body
      res.contains("VAT-registration thresholds can change. Recent ones include:") mustBe true
      res.contains("1 April 2017 to present: £85,000") mustBe true
      res.contains("1 April 2016 to 31 March 2017: £83,000") mustBe true
      res.contains("1 April 2015 and 31 March 2016: £82,000") mustBe true

    }
    "returnHelpText1Previous should return default text for none incorporated companies" in new Setup(None) {
      val res = service.returnThresholdDateResult(service.returnHelpText1Previous).body
      res.contains("VAT-registration thresholds can change. Recent ones include:") mustBe true
      res.contains("1 April 2017 to present: £85,000") mustBe true
      res.contains("1 April 2016 to 31 March 2017: £83,000") mustBe true
      res.contains("1 April 2015 and 31 March 2016: £82,000") mustBe true
      res.contains("Before this, use these") mustBe true
      res.contains("https://www.gov.uk/government/publications/vat-notice-7001-should-i-be-registered-for-vat/vat-notice-7001-supplement--2#registration-limits-taxable-supplies") mustBe true
      res.contains("previous VAT registration thresholds (opens in a new window or tab)") mustBe true
    }
    "returnHelpText1Previous should return default text for companies incorped < 2015" in new Setup(Some(LocalDate.of(2013, 1, 1))) {
      val res = service.returnThresholdDateResult(service.returnHelpText1Previous).body
      res.contains("VAT-registration thresholds can change. Recent ones include:") mustBe true
      res.contains("1 April 2017 to present: £85,000") mustBe true
      res.contains("1 April 2016 to 31 March 2017: £83,000") mustBe true
      res.contains("1 April 2015 and 31 March 2016: £82,000") mustBe true
      res.contains("Before this, use these") mustBe true
      res.contains("https://www.gov.uk/government/publications/vat-notice-7001-should-i-be-registered-for-vat/vat-notice-7001-supplement--2#registration-limits-taxable-supplies") mustBe true
      res.contains("previous VAT registration thresholds (opens in a new window or tab)") mustBe true
    }
    "returnHeadingPrevious should return limitedIncorpedEqualOrAfter20170401 text" in new Setup(Some(LocalDate.of(2017, 4, 1))) {
      service.returnThresholdDateResult(service.returnHeadingPrevious) mustBe "Has Test Company ever expected to make more than £85,000 in VAT-taxable sales in a single 30-day period?"
    }
    "returnHeadingPrevious should return normal heading for unincorped company" in new Setup(None) {
      service.returnThresholdDateResult(service.returnHeadingPrevious) mustBe "Has Test Company ever expected to go over the VAT-registration threshold in a single 30-day period?"
    }
    "returnHeadingPrevious should return normal heading for company incorped < 20170401" in new Setup(Some(LocalDate.of(2017, 3, 31))) {
      service.returnThresholdDateResult(service.returnHeadingPrevious) mustBe "Has Test Company ever expected to go over the VAT-registration threshold in a single 30-day period?"
    }
  }
}