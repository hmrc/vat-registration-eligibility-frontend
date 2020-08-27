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

import controllers.routes
import deprecated.DeprecatedConstants
import forms.InternationalActivitiesFormProvider
import models.NormalMode
import play.api.data.Form
import views.newbehaviours.YesNoViewBehaviours
import views.html.internationalActivities

class InternationalActivitiesViewSpec extends YesNoViewBehaviours {

  val extraParamForLegend: String = DeprecatedConstants.fakeCompanyName
  val messageKeyPrefix = "internationalActivities"
  val form = new InternationalActivitiesFormProvider()()
  implicit val msgs = messages

  def createView = () => internationalActivities(form, NormalMode)(fakeDataRequestIncorped, messages, frontendAppConfig)

  def createViewUsingForm = (form: Form[_]) => internationalActivities(form, NormalMode)(fakeDataRequestIncorped, messages, frontendAppConfig)

  "InternationalActivities view" must {
    behave like normalPage(createView(), messageKeyPrefix, Seq(extraParamForLegend))
    behave like yesNoPage(form, createViewUsingForm, messageKeyPrefix, routes.InternationalActivitiesController.onSubmit().url, Seq(extraParamForLegend))
  }

}
