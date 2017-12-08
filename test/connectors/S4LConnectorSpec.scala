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

package connectors

import config.VatShortLivedCache
import helpers.FutureAssertions
import models.view.TaxableTurnover
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future
import uk.gov.hmrc.http.{ HeaderCarrier, HttpResponse }

class S4LConnectorSpec extends UnitSpec with MockitoSugar with FutureAssertions {

  val mockShortLivedCache = mock[VatShortLivedCache]

  val s4lConnectorTest = new S4LConnector {
    override val shortCache = mockShortLivedCache
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val taxableTurnoverModel = TaxableTurnover(TaxableTurnover.TAXABLE_NO)
  val cacheMap = CacheMap("", Map("" -> Json.toJson(taxableTurnoverModel)))

  "Fetching from save4later" should {
    "return the correct model" in {

      when(mockShortLivedCache.fetchAndGetEntry[TaxableTurnover](ArgumentMatchers.anyString(), ArgumentMatchers.anyString())
        (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Option(taxableTurnoverModel)))

      await(s4lConnectorTest.fetchAndGet[TaxableTurnover]("", "")) shouldBe Some(taxableTurnoverModel)
    }
  }

  "Saving a model into save4later" should {
    "save the model" in {
      val returnCacheMap = CacheMap("", Map("" -> Json.toJson(taxableTurnoverModel)))

      when(mockShortLivedCache.cache[TaxableTurnover](ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.any())
        (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(returnCacheMap))

      val result = s4lConnectorTest.save[TaxableTurnover]("", "", taxableTurnoverModel)
      await(result) shouldBe returnCacheMap
    }
  }

  "clearing an entry using save4later" should {
    "clear the entry given the user id" in {
      when(mockShortLivedCache.remove(ArgumentMatchers.anyString())
        (ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK)))

      val result = s4lConnectorTest.clear("test")
      await(result).status shouldBe HttpResponse(OK).status
    }
  }

  "fetchAll" should {
    "fetch all entries in S4L" in {
      when(mockShortLivedCache.fetch(ArgumentMatchers.any())
        (ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(cacheMap)))

      val result = s4lConnectorTest.fetchAll("testUserId")
      await(result) shouldBe Some(cacheMap)
    }
  }
}
