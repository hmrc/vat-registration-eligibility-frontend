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

package models.views

import java.time.LocalDate

import fixtures.VatRegistrationFixture
import models.api.VatThresholdPostIncorp
import models.view.OverThresholdView
import models.{ApiModelTransformer, MonthYearModel, S4LVatEligibilityChoice}
import org.scalatest.Inside
import uk.gov.hmrc.play.test.UnitSpec

class OverThresholdViewSpec extends UnitSpec with VatRegistrationFixture with Inside {

  val date = LocalDate.of(2017, 3, 31)
  val yesView = OverThresholdView(true, Some(date))
  val noView = OverThresholdView(false, None)

  "unbind" should {
    "decompose an over threshold view with a date" in {
      inside(OverThresholdView.unbind(yesView)) {
        case Some((selection, otDate)) =>
          selection shouldBe true
          otDate shouldBe Some(MonthYearModel.fromLocalDate(date))
      }
    }

    "decompose an over threshold view without a date" in {
      inside(OverThresholdView.unbind(noView)) {
        case Some((selection, otDate)) =>
          selection shouldBe false
          otDate shouldBe None
      }
    }
  }

  "bind" should {
    "create OverThresholdView when MonthYearModel is present" in {
      OverThresholdView.bind(true, Some(MonthYearModel.fromLocalDate(date))) shouldBe OverThresholdView(true, Some(date))
    }
    "create OverThresholdView when MonthYearModel is NOT present" in {
      OverThresholdView.bind(false, None) shouldBe OverThresholdView(false, None)
    }
  }

  "ViewModelFormat" should {
    val validOverThresholdView = OverThresholdView(false, None)
    val s4LVatChoice: S4LVatEligibilityChoice = S4LVatEligibilityChoice(overThreshold = Some(validOverThresholdView))

    "extract over threshold from vatChoice" in {
      OverThresholdView.viewModelFormat.read(s4LVatChoice) shouldBe Some(validOverThresholdView)
    }

    "update empty vatChoice with over threshold" in {
      OverThresholdView.viewModelFormat.update(validOverThresholdView, Option.empty[S4LVatEligibilityChoice]).overThreshold shouldBe Some(validOverThresholdView)
    }

    "update non-empty vatChoice with over threshold" in {
      OverThresholdView.viewModelFormat.update(validOverThresholdView, Some(s4LVatChoice)).overThreshold shouldBe Some(validOverThresholdView)
    }

    "ApiModelTransformer" should {

      "produce empty view model from an empty frs start date" in {
        val vm = ApiModelTransformer[OverThresholdView]
          .toViewModel(vatScheme(vatServiceEligibility = None))
        vm shouldBe None
      }

      "produce a view model from a vatScheme with an over threshold date set" in {
        val vm = ApiModelTransformer[OverThresholdView]
          .toViewModel(vatScheme(vatServiceEligibility = Some(validServiceEligibility.copy(
            vatEligibilityChoice = Some(validVatChoice.copy(vatThresholdPostIncorp =
              Some(VatThresholdPostIncorp(overThresholdSelection = true, overThresholdDate = Some(date)))))
          ))))
        vm shouldBe Some(OverThresholdView(true, Some(date)))
      }

      "produce a view model from a vatScheme with no over threshold date" in {
        val vm = ApiModelTransformer[OverThresholdView]
          .toViewModel(vatScheme(vatServiceEligibility = Some(validServiceEligibility.copy(
            vatEligibilityChoice = Some(validVatChoice.copy(vatThresholdPostIncorp =
              Some(VatThresholdPostIncorp(overThresholdSelection = false, None))))
          ))))
        vm shouldBe Some(OverThresholdView(false, None))
      }

    }
  }

}
