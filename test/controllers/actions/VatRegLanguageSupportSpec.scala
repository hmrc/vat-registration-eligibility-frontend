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

package controllers.actions

import base.SpecBase
import config.FrontendAppConfig
import featureswitch.core.config.{FeatureSwitching, WelshLanguage}
import models.requests.CacheIdentifierRequest
import org.jsoup.Jsoup
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.AgriculturalDropout

import javax.inject.Inject

class VatRegLanguageSupportSpec extends SpecBase with FeatureSwitching {

  object ExpectedMessages {
    val welshHeading = "Mae’n rhaid i’r busnes gofrestru ar gyfer TAW gan ddefnyddio gwasanaeth gwahanol"
    val englishHeading = "The business must register for VAT using a different service"
  }

  val testController = app.injector.instanceOf[WelshTestController]

  "VAT Reg Language Support" when {
    "the play lang cookie is set to Welsh" when {
      "the Welsh feature switch is enabled" must {
        "return a page with Welsh content" in {
          enable(WelshLanguage)

          val res = testController.getPage()(FakeRequest().withCookies(Cookie("PLAY_LANG", "cy")))
          val doc = Jsoup.parse(contentAsString(res))

          doc.select("h1").first().text() mustBe ExpectedMessages.welshHeading
        }
      }
      "the Welsh feature switch is disabled" must {
        "return a page with English content" in {
          disable(WelshLanguage)

          val res = testController.getPage()(FakeRequest().withCookies(Cookie("PLAY_LANG", "cy")))
          val doc = Jsoup.parse(contentAsString(res))

          doc.select("h1").first().text() mustBe ExpectedMessages.englishHeading
        }
      }
    }
    "the play lang cookie is set to English" when {
      "the Welsh feature switch is enabled" must {
        "return a page with English content" in {
          enable(WelshLanguage)

          val res = testController.getPage()(FakeRequest().withCookies(Cookie("PLAY_LANG", "en")))
          val doc = Jsoup.parse(contentAsString(res))

          doc.select("h1").first().text() mustBe ExpectedMessages.englishHeading
        }
      }
      "the Welsh feature switch is disabled" must {
        "return a page with English content" in {
          disable(WelshLanguage)

          val res = testController.getPage()(FakeRequest().withCookies(Cookie("PLAY_LANG", "en")))
          val doc = Jsoup.parse(contentAsString(res))

          doc.select("h1").first().text() mustBe ExpectedMessages.englishHeading
        }
      }
    }
  }

}

class WelshTestController @Inject()(page: AgriculturalDropout,
                                    val controllerComponents: ControllerComponents)
                                   (implicit appConfig: FrontendAppConfig) extends BaseController with VatRegLanguageSupport {

  def getPage: Action[AnyContent] = Action { request =>
    implicit val cacheRequest: CacheIdentifierRequest[AnyContent] = CacheIdentifierRequest(request, "1", "1")
    Ok(page())
  }

}
