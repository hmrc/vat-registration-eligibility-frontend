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

package utils

import base.SpecBase
import controllers.routes
import identifiers._
import models._
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.JsBoolean
import play.api.mvc.Call
import uk.gov.hmrc.http.cache.client.CacheMap

class NavigatorSpec extends SpecBase with MockitoSugar {

  val navigator = new Navigator

  "Navigator" when {
    "in Normal mode" must {
      "go to Index from an identifier that doesn't exist in the route map" in {
        case object UnknownIdentifier extends Identifier
        navigator.nextPage(UnknownIdentifier, NormalMode)(mock[UserAnswers]) mustBe routes.ThresholdNextThirtyDaysController.onPageLoad()
      }
    }
  }

  "pageIdToPageLoad" should {
    "load a page" when {
      Seq[(Identifier, Call)](

      ) foreach { case (id, page) =>
        s"given an ID of ${id.toString} should go to ${page.url}" in {
          navigator.pageIdToPageLoad(id).url must include(page.url)
        }
      }
    }

    "throw a run time exception" when {
      "given an invalid ID" in {
        sealed class FakeID extends Identifier

        intercept[RuntimeException](navigator.pageIdToPageLoad(new FakeID))
      }
    }
  }

//  "nextOnFalse" should {
//    "return an ID and function to the next page" when {
//      "given a start page id and end page id when the answer provided is false" in {
//        val res = navigator.nextOnFalse(TooManyDirectorsId, OrdinarySharesId)
//        val userAnswers = new UserAnswers(CacheMap("id", Map(TooManyDirectorsId.toString -> JsBoolean(false))))
//
//        res._1 mustBe TooManyDirectorsId
//        res._2(userAnswers) mustBe routes.OrdinarySharesController.onPageLoad(NormalMode)
//      }
//
  //    }
//
//    "return an ID and function to the ineligible" when {
//      "given a start page id and end page id when the answer provided is true" in {
//        val res = navigator.nextOnFalse(TooManyDirectorsId, OrdinarySharesId)
//        val userAnswers = new UserAnswers(CacheMap("id", Map(TooManyDirectorsId.toString -> JsBoolean(true))))
//
//        res._1 mustBe TooManyDirectorsId
//        res._2(userAnswers) mustBe routes.IneligibleController.onPageLoad(TooManyDirectorsId.toString)
//      }
//    }
//  }
}
