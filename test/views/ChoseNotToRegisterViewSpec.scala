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

package views

import deprecated.DeprecatedConstants
import play.api.i18n.Messages
import views.newbehaviours.ViewBehaviours
import views.html.choseNotToRegister

class ChoseNotToRegisterViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "choseNotToRegister"
  implicit val msgs = messages

  def createView = () => choseNotToRegister()(fakeCacheDataRequestIncorped, messages, frontendAppConfig)

  "ChoseNotToRegister view" must {
    behave like normalPage(createView(), messageKeyPrefix, Seq(DeprecatedConstants.fakeCompanyName))
  }
}
