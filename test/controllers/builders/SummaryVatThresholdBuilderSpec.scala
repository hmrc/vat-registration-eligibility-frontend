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

package controllers.builders

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.view.{ExpectationOverThresholdView, OverThresholdView, SummaryRow, SummarySection}


class   SummaryVatThresholdBuilderSpec extends VatRegSpec with VatRegistrationFixture {

  val specificDate = LocalDate.of(2017, 11, 12)

  val monthYearPresentationFormatter = DateTimeFormatter.ofPattern("MMMM y")

  "building a Summary vat threshold row" should {
    val validOtherThresholdPostIncorp = validThresholdPostIncorp.copy(
      overThreshold = Some(OverThresholdView(true, Some(specificDate))),
      expectationOverThreshold = Some(ExpectationOverThresholdView(true, Some(specificDate)))
    )
    "with ThresholdSelectionRow" should {
      "return a summary row with 'YES' if overThreshold is true" in {
        val postIncorpBuilder = SummaryVatThresholdBuilder(validOtherThresholdPostIncorp)

        postIncorpBuilder.overThresholdSelectionRow shouldBe SummaryRow(
          "threshold.overThresholdSelection",
          "app.common.yes",
          Some(controllers.routes.ThresholdController.goneOverShow())
        )
      }

      "return a summary row with 'NO' if overThreshold is FALSE" in {
        val noPostIncorpBuilder = SummaryVatThresholdBuilder(validThresholdPostIncorp)
        noPostIncorpBuilder.overThresholdSelectionRow shouldBe SummaryRow(
          "threshold.overThresholdSelection",
          "app.common.no",
          Some(controllers.routes.ThresholdController.goneOverShow())
        )
      }
    }

    "with overThresholdDateRow" should {
      "return a summary row with 'November 2017' if a date is given" in {
        val postIncorpBuilder = SummaryVatThresholdBuilder(validOtherThresholdPostIncorp)
        postIncorpBuilder.overThresholdDateRow shouldBe SummaryRow(
          "threshold.overThresholdDate",
          specificDate.format(monthYearPresentationFormatter),
          Some(controllers.routes.ThresholdController.goneOverShow())
        )
      }

      "return a summary row with '' if no date is given" in {
        val noPostIncorpBuilder = SummaryVatThresholdBuilder(validThresholdPostIncorp)
        noPostIncorpBuilder.overThresholdDateRow shouldBe SummaryRow(
          "threshold.overThresholdDate",
          "",
          Some(controllers.routes.ThresholdController.goneOverShow())
        )
      }

      "Section" should {
        "show the rows if overThreshold and ExpectedOverThreshold are true for both Questions" in {
          val postIncorpBuilder = SummaryVatThresholdBuilder(validOtherThresholdPostIncorp)
          postIncorpBuilder.section shouldBe SummarySection(
            "threshold",
            Seq((postIncorpBuilder.overThresholdSelectionRow, true),
              (postIncorpBuilder.overThresholdDateRow, true),
              (postIncorpBuilder.expectedOverThresholdSelectionRow,true),
              (postIncorpBuilder.expectedOverThresholdDateRow, true))
          )
        }

        "hide the rows if overThreshold and ExpectedOverThreshold are false for both questions" in {
          val noPostIncorpBuilder = SummaryVatThresholdBuilder(validThresholdPostIncorp)
          noPostIncorpBuilder.section shouldBe SummarySection(
            "threshold",
            Seq((noPostIncorpBuilder.overThresholdSelectionRow, true),
              (noPostIncorpBuilder.overThresholdDateRow, false),
              (noPostIncorpBuilder.expectedOverThresholdSelectionRow,true),
              (noPostIncorpBuilder.expectedOverThresholdDateRow,false))
          )
        }
      }
    }
  }
}
