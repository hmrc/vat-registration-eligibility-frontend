/*
 * Copyright 2022 HM Revenue & Customs
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

import forms.DateOfBusinessTransferFormProvider
import models.NormalMode
import utils.TimeMachine
import views.html.DateOfBusinessTransfer

import java.time.LocalDate

class DateOfBusinessTransferViewSpec extends ViewSpecBase {

  val messageKeyPrefix = "dateOfBusinessTransfer"

  object TestTimeMachine extends TimeMachine {
    override def today: LocalDate = LocalDate.parse("2020-01-01")
  }

  object Selectors extends BaseSelectors

  val h1Togc = "What date did the transfer of business take place?"
  val h1Cole = "What date did the change of legal entity take place?"
  val paraTogc = "The date of transfer must match the date the previous owner of the business gave us."
  val paraCole = "The change of legal entity date must match the date the previous entity gave us."
  val testHint = "For example, 27 03 2007"
  val testButton = "Continue"
  val togc = "togc"
  val cole = "cole"

  val view = app.injector.instanceOf[DateOfBusinessTransfer]

  "DateOfBusinessTransfer view" when {
    s"registration reason is $togc" must {
      val doc =
        asDocument(view
          (new DateOfBusinessTransferFormProvider(TestTimeMachine)(togc), NormalMode, togc)
          (fakeDataRequestIncorped, messages, frontendAppConfig)
        )

      "have the correct continue button" in {
        doc.select(Selectors.button).text() mustBe continueButton
      }

      "have the correct back link" in {
        doc.select(Selectors.backLink).text() mustBe backLink
      }

      "have the correct browser title" in {
        doc.select(Selectors.title).text() mustBe title(h1Togc)
      }

      "have the correct heading" in {
        doc.select(Selectors.h1).text() mustBe h1Togc
      }

      "have the correct legend" in {
        doc.select(Selectors.legend(1)).text() mustBe h1Togc
      }

      "have the correct paragraph" in {
        doc.select(Selectors.p(1)).text() mustBe paraTogc
      }

      "have the correct hint" in {
        doc.select(Selectors.hint).text() mustBe testHint
      }
    }

    s"registration reason is $cole" must {
      val doc =
        asDocument(view
        (new DateOfBusinessTransferFormProvider(TestTimeMachine)(cole), NormalMode, cole)
        (fakeDataRequestIncorped, messages, frontendAppConfig)
        )

      "have the correct continue button" in {
        doc.select(Selectors.button).text() mustBe continueButton
      }

      "have the correct back link" in {
        doc.select(Selectors.backLink).text() mustBe backLink
      }

      "have the correct browser title" in {
        doc.select(Selectors.title).text() mustBe title(h1Cole)
      }

      "have the correct heading" in {
        doc.select(Selectors.h1).text() mustBe h1Cole
      }

      "have the correct legend" in {
        doc.select(Selectors.legend(1)).text() mustBe h1Cole
      }

      "have the correct paragraph" in {
        doc.select(Selectors.p(1)).text() mustBe paraCole
      }

      "have the correct hint" in {
        doc.select(Selectors.hint).text() mustBe testHint
      }
    }
  }
}
