/*
 * Copyright 2026 HM Revenue & Customs
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
import config.FrontendAppConfig
import featureswitch.core.config.FeatureSwitching
import featureswitch.core.models.FeatureSwitch.UrBannerEnabled
import play.api.i18n.Lang
import uk.gov.hmrc.hmrcfrontend.views.Aliases.{Cy, En, UserResearchBanner}

class UrBannerUtilSpec extends SpecBase with FeatureSwitching {

  "getUrBanner" when {
    val appConfig = app.injector.instanceOf[FrontendAppConfig]

    "the showUserResearchBanner feature is disabled" should {

      "return None" in {
        disable(UrBannerEnabled)
        UrBannerUtil.getUrBanner()(appConfig, messages) mustBe None
      }
    }

    "the showUserResearchBanner feature is enabled" when {

      "the language is English" should {

        "return a UserResearchBanner with En language and the English URL" in {
          enable(UrBannerEnabled)
          UrBannerUtil.getUrBanner()(appConfig, messages) mustBe Some(UserResearchBanner(
            language = En,
            url = appConfig.urBannerBaseUrl,
            hideCloseButton = true
          ))
        }
      }

      "the language is Welsh" should {

        "return a UserResearchBanner with Cy language and the Welsh URL" in {
          enable(UrBannerEnabled)
          val welshMessages = messagesApi.preferred(Seq(Lang("cy")))
          UrBannerUtil.getUrBanner()(appConfig, welshMessages) mustBe Some(UserResearchBanner(
            language = Cy,
            url = s"${appConfig.urBannerBaseUrl}&Q_Language=CY",
            hideCloseButton = true
          ))
        }
      }

      "hideCloseButton is false" should {

        "return a UserResearchBanner with hideCloseButton set to false" in {
          enable(UrBannerEnabled)
          val result = UrBannerUtil.getUrBanner(hideCloseButton = false)(appConfig, messages)
          result.map(_.hideCloseButton) mustBe Some(false)
        }
      }
    }
  }
}