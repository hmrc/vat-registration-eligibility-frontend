/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.BusinessEntityFormProvider
import views.html.BusinessEntityOverseas

class BusinessEntityOverseasViewSpec extends ViewSpecBase {

  val messageKeyPrefix = "BusinessEntityOverseas"
  val form = new BusinessEntityFormProvider()()
  val view = app.injector.instanceOf[BusinessEntityOverseas]

  object Selectors extends BaseSelectors

  val h1 = "What type of business do you want to register for VAT?"
  val radio1 = "Non-established taxable person (NETP)"
  val radio2 = "Non UK Company"
  val hint = "An NETP is any person who is not normally resident in the UK, does not have a UK establishment and, in the case of a company, is not incorporated in the UK."

  "BusinessEntityOverseas view" must {
    lazy val doc = asDocument(view(form, routes.BusinessEntityOverseasController.onSubmit())(fakeDataRequestIncorped, messages, frontendAppConfig))

    "have the correct continue button" in {
      doc.select(Selectors.button).text() mustBe continueButton
    }

    "have the correct back link" in {
      doc.getElementById(Selectors.backLink).text() mustBe backLink
    }

    "have the correct browser title" in {
      doc.select(Selectors.title).text() mustBe title(h1)
    }

    "have the correct heading" in {
      doc.select(Selectors.h1).text() mustBe h1
    }

    "have the right radio options" in {
      doc.select(Selectors.radio(1)).text() mustBe radio1
      doc.select(Selectors.radio(2)).text() mustBe radio2
    }
    "have the right hint" in {
      doc.select(Selectors.hint).text() mustBe hint
    }
  }
}
