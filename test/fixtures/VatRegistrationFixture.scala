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

package fixtures

import java.time.LocalDate

import models.external.{IncorporationInfo, _}
import models.view.TaxableTurnover._
import models.view.VoluntaryRegistration._
import models.view.VoluntaryRegistrationReason._
import models.view._

trait VatRegistrationFixture {
  //Test variables
  val testRegId = "VAT123456"
  val testDate = Some(LocalDate.of(2017, 3, 21))

  //Api models
  val validThresholdPreIncorp = Threshold(
    taxableTurnover = Some(TaxableTurnover(TAXABLE_NO)),
    voluntaryRegistration = Some(VoluntaryRegistration(REGISTER_YES)),
    voluntaryRegistrationReason = Some(VoluntaryRegistrationReason(SELLS)),
    overThreshold = None,
    expectationOverThreshold = None
  )
  val validThresholdPostIncorp = Threshold(
    taxableTurnover = None,
    voluntaryRegistration = Some(VoluntaryRegistration(REGISTER_YES)),
    voluntaryRegistrationReason = Some(VoluntaryRegistrationReason(SELLS)),
    overThreshold = Some(OverThresholdView(selection = false, None)),
    expectationOverThreshold = Some(ExpectationOverThresholdView(selection = false, None))
  )
  val validThresholdPostIncorp2 = Threshold(
    taxableTurnover = None,
    voluntaryRegistration = Some(VoluntaryRegistration(REGISTER_YES)),
    voluntaryRegistrationReason = Some(VoluntaryRegistrationReason(SELLS)),
    overThreshold = Some(OverThresholdView(selection = true, testDate)),
    expectationOverThreshold = Some(ExpectationOverThresholdView(selection = true, testDate))
  )

  val validEligibility = Eligibility(Some(true),Some(false),Some(false),Some(false),Some(false),Some(false))

  // View Models
  val validOverThresholdView = OverThresholdView(false,None)
  val validTaxableTurnOverView = TaxableTurnover(TaxableTurnover.TAXABLE_YES)
  val validVoluntaryRegistrationView = VoluntaryRegistration(VoluntaryRegistration.REGISTER_YES)
  val validVoluntaryRegistrationReasonView = VoluntaryRegistrationReason(VoluntaryRegistrationReason.SELLS)

  val testIncorporationInfo = IncorporationInfo(
    IncorpSubscription(
      transactionId = "000-434-23",
      regime = "vat",
      subscriber = "scrs",
      callbackUrl = "http://localhost:9896/TODO-CHANGE-THIS"),
    IncorpStatusEvent(
      status = "accepted",
      crn = Some("90000001"),
      incorporationDate = Some(LocalDate.of(2016, 8, 5)),
      description = Some("Some description")))

  val nationalInsuranceSection = SummarySection(
    "nationalInsurance",
    rows = Seq((SummaryRow("nationalInsurance.hasNino","app.common.yes",Some(controllers.routes.EligibilityController.showHaveNino())),true))
  )

  val internationalBusinessSection = SummarySection(
    "internationalBusiness",
    rows = Seq(
      (SummaryRow("internationalBusiness.sellGoods","app.common.no",Some(controllers.routes.EligibilityController.showDoingBusinessAbroad())),true),
      (SummaryRow("internationalBusiness.buyGoods","app.common.no",Some(controllers.routes.EligibilityController.showDoingBusinessAbroad())),true),
      (SummaryRow("internationalBusiness.sellAssets","app.common.no",Some(controllers.routes.EligibilityController.showDoingBusinessAbroad())),true),
      (SummaryRow("internationalBusiness.sellGoodsServices","app.common.no",Some(controllers.routes.EligibilityController.showDoingBusinessAbroad())),true),
      (SummaryRow("internationalBusiness.doBusiness","app.common.no",Some(controllers.routes.EligibilityController.showDoingBusinessAbroad())),true)
    )
  )

  val otherBusinessSection = SummarySection(
    "otherBusiness",
    rows = Seq(
      (SummaryRow("otherBusiness.soleTrader","app.common.no",Some(controllers.routes.EligibilityController.showDoAnyApplyToYou())),true),
      (SummaryRow("otherBusiness.vatGroup","app.common.no",Some(controllers.routes.EligibilityController.showDoAnyApplyToYou())),true),
      (SummaryRow("otherBusiness.makingProfit","app.common.no",Some(controllers.routes.EligibilityController.showDoAnyApplyToYou())),true),
      (SummaryRow("otherBusiness.limitedCompany","app.common.no",Some(controllers.routes.EligibilityController.showDoAnyApplyToYou())),true)
    )
  )

  val otherVatSchemeSection = SummarySection(
    "otherVatScheme",
    rows = Seq(
      (SummaryRow("otherVatScheme.agriculturalFlat","app.common.no",Some(controllers.routes.EligibilityController.showApplyingForAnyOf())),true),
      (SummaryRow("otherVatScheme.accountingScheme","app.common.no",Some(controllers.routes.EligibilityController.showApplyingForAnyOf())),true)
    )
  )

  val vatExemptionSection = SummarySection(
    "vatExemption",
    rows = Seq(
      (SummaryRow("vatExemption.vatException","app.common.no",Some(controllers.routes.EligibilityController.showExemptionCriteria())),true),
      (SummaryRow("vatExemption.vatExemption","app.common.no",Some(controllers.routes.EligibilityController.showExemptionCriteria())),true)
    )
  )

  val resourcesSection = SummarySection(
    "resources",
    rows = Seq(
      (SummaryRow("resources.companyOwn","app.common.no",Some(controllers.routes.EligibilityController.showCompanyWillDoAnyOf())),true),
      (SummaryRow("resources.companySell","app.common.no",Some(controllers.routes.EligibilityController.showCompanyWillDoAnyOf())),true)
    )
  )

  val validEligibilitySummary = Summary(Seq(
    nationalInsuranceSection,
    internationalBusinessSection,
    otherBusinessSection,
    otherVatSchemeSection,
    vatExemptionSection,
    resourcesSection
  ))
}
