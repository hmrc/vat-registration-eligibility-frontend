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

package transformers

import java.time.LocalDate

import models.view.{TaxableTurnover,
                    Threshold,
                    VoluntaryRegistration => VolReg,
                    VoluntaryRegistrationReason => VolRegReason,
                    OverThresholdView => OverThres,
                    ExpectationOverThresholdView => ExpOverThres}
import models.view.TaxableTurnover._
import models.view.VoluntaryRegistration._
import play.api.libs.json.JsValue

object ToThresholdView {
  private def toEligibilityChoiceViewModelWithoutIncorpDate(api: JsValue): Threshold = {
    Threshold(
      taxableTurnover = Some(
        if ((api \ "mandatoryRegistration").validate[Boolean].get) TaxableTurnover(TAXABLE_YES) else TaxableTurnover(TAXABLE_NO)
      ),
      voluntaryRegistration = Some(
        if ((api \ "mandatoryRegistration").validate[Boolean].get) VolReg(REGISTER_NO) else VolReg(REGISTER_YES)
      ),
      voluntaryRegistrationReason = (api \ "voluntaryReason").validateOpt[String].get map (VolRegReason(_)),
      overThreshold = None,
      expectationOverThreshold = None
    )
  }

  private def toEligibilityChoiceViewModelWithIncorpDate(api: JsValue): Threshold = {
    Threshold(
      taxableTurnover = None,
      voluntaryRegistration = Some(
        if ((api \ "mandatoryRegistration").validate[Boolean].get) VolReg(REGISTER_NO) else VolReg(REGISTER_YES)
      ),
      voluntaryRegistrationReason = (api \ "voluntaryReason").validateOpt[String].get map (VolRegReason(_)),
      overThreshold = Some(
        (api \ "overThresholdDate").validateOpt[LocalDate].get.fold(OverThres(false, None))(d => OverThres(true, Some(d)))
      ),
      expectationOverThreshold = Some(
        (api \ "expectedOverThresholdDate").validateOpt[LocalDate].get.fold(ExpOverThres(selection = false, None))(d => ExpOverThres(selection = true, Some(d)))
      )
    )
  }

  def fromAPI(api: JsValue, isIncorporated: Boolean): Threshold = {
    if (isIncorporated) toEligibilityChoiceViewModelWithIncorpDate(api) else toEligibilityChoiceViewModelWithoutIncorpDate(api)
  }
}
