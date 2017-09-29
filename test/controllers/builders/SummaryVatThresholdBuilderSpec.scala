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
import models.api.VatThresholdPostIncorp
import models.view.{SummaryRow, SummarySection}

/**
  * Created by eric on 26/09/17.
  */
class   SummaryVatThresholdBuilderSpec extends VatRegSpec with VatRegistrationFixture {

  val specificDate = LocalDate.of(2017, 11, 12)

  val monthYearPresentationFormatter = DateTimeFormatter.ofPattern("MMMM y")

  "building a Summary vat threshould row" should {

    "with overThresholdSelectionRow" should {

      "return a summary row with 'YES' if post incorp is true" in {
        val postIncorpBuilder = SummaryVatThresholdBuilder(Some(VatThresholdPostIncorp(true,Some(specificDate))))
        postIncorpBuilder.overThresholdSelectionRow shouldBe SummaryRow(
          "threshold.overThresholdSelection",
          "app.common.yes",
          Some(controllers.routes.OverThresholdController.show())
        )
      }

      "return a summary row with 'NO' if post incorp is FALSE" in {
        val noPostIncorpBuilder = SummaryVatThresholdBuilder(Some(VatThresholdPostIncorp(false, None)))
        noPostIncorpBuilder.overThresholdSelectionRow shouldBe SummaryRow(
          "threshold.overThresholdSelection",
          "app.common.no",
          Some(controllers.routes.OverThresholdController.show())
        )
      }
    }

    "with overThresholdDateRow" should {

      "return a summary row with 'November 2017' if a date is given" in {
        val postIncorpBuilder = SummaryVatThresholdBuilder(Some(VatThresholdPostIncorp(true,Some(specificDate))))
        postIncorpBuilder.overThresholdDateRow shouldBe SummaryRow(
          "threshold.overThresholdDate",
          specificDate.format(monthYearPresentationFormatter),
          Some(controllers.routes.OverThresholdController.show())
        )
      }

      "return a summary row with '' if no date is given" in {
        val noPostIncorpBuilder = SummaryVatThresholdBuilder(Some(VatThresholdPostIncorp(false, None)))
        noPostIncorpBuilder.overThresholdDateRow shouldBe SummaryRow(
          "threshold.overThresholdDate",
          "",
          Some(controllers.routes.OverThresholdController.show())
        )
      }

      "Section" should {

        "show the rows if post incorp is true" in {
          val postIncorpBuilder = SummaryVatThresholdBuilder(Some(VatThresholdPostIncorp(true, Some(specificDate))))
          postIncorpBuilder.section shouldBe SummarySection(
            "threshold",
            Seq((postIncorpBuilder.overThresholdSelectionRow, true),
              (postIncorpBuilder.overThresholdDateRow, true))
          )
        }

        "hide the rows if post incorp is false" in {
          val noPostIncorpBuilder = SummaryVatThresholdBuilder(Some(VatThresholdPostIncorp(false, None)))
          noPostIncorpBuilder.section shouldBe SummarySection(
            "threshold",
            Seq((noPostIncorpBuilder.overThresholdSelectionRow, true),
              (noPostIncorpBuilder.overThresholdDateRow, false))
          )
        }
      }
    }
  }
}
