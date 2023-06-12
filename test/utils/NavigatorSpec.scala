/*
 * Copyright 2023 HM Revenue & Customs
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
import identifiers._
import models._
import play.api.libs.json.JsValue
import play.api.mvc.Call
import uk.gov.hmrc.http.cache.client.CacheMap

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator
  val testId = "testId"

  def newCacheMap(map: Map[String, JsValue]) = new UserAnswers(CacheMap(testId, map))

  "Navigator" when {
    "in Normal mode" must {
      "go to Index from an identifier that doesn't exist in the route map" in {
        case object UnknownIdentifier extends Identifier
        navigator.nextPage(UnknownIdentifier, NormalMode)(request)(mock[UserAnswers]) mustBe controllers.routes.FixedEstablishmentController.onPageLoad
      }
    }
  }

  "pageIdToPageLoad" must {
    "load a page" when {
      Seq[(Identifier, Call)](

      ) foreach { case (id, page) =>
        s"given an ID of ${id.toString} should go to ${page.url}" in {
          navigator.pageIdToPageLoad(id).url must include(page.url)
        }
      }
    }

    "redirect to the start of the VAT EL Flow" when {
      "given an invalid ID" in {
        val fakeId = new Identifier {
          override def toString: String = "fudge"
        }
        navigator.pageIdToPageLoad(fakeId).url mustBe controllers.routes.FixedEstablishmentController.onPageLoad.url
      }
    }
  }
}
