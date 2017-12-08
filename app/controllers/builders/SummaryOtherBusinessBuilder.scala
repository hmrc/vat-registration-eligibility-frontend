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

import models.view.{SummaryRow, SummarySection}

case class SummaryOtherBusinessBuilder(a:Option[Boolean])
  extends SummarySectionBuilder {

  override val sectionId: String = "otherBusiness"

  val soleTraderRow: SummaryRow = SummaryRow(
    s"$sectionId.soleTrader",
    booleanToMessageKey(a),
    Some(controllers.routes.EligibilityController.showDoAnyApplyToYou())
  )

  val vatGroupRow: SummaryRow = SummaryRow(
    s"$sectionId.vatGroup",
    booleanToMessageKey(a),
    Some(controllers.routes.EligibilityController.showDoAnyApplyToYou())
  )

  val makingProfitRow: SummaryRow = SummaryRow(
    s"$sectionId.makingProfit",
    booleanToMessageKey(a),
    Some(controllers.routes.EligibilityController.showDoAnyApplyToYou())
  )

  val limitedCompanyRow: SummaryRow = SummaryRow(
    s"$sectionId.limitedCompany",
    booleanToMessageKey(a),
    Some(controllers.routes.EligibilityController.showDoAnyApplyToYou())
  )

  val section: SummarySection = SummarySection(
    sectionId,
    rows = Seq(
      (soleTraderRow, true),
      (vatGroupRow, true),
      (makingProfitRow, true),
      (limitedCompanyRow, true)
    )
  )
}
