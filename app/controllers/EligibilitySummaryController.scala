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

package controllers

import javax.inject.Inject

import play.api.i18n.MessagesApi
import play.api.mvc._
import services.{CurrentProfileService, SummaryService, VatRegistrationService}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.SessionProfile

class EligibilitySummaryControllerImpl @Inject()(val messagesApi: MessagesApi,
                                                 val summaryService: SummaryService,
                                                 val vatRegistrationService: VatRegistrationService,
                                                 val authConnector: AuthConnector,
                                                 val currentProfileService: CurrentProfileService) extends EligibilitySummaryController

trait EligibilitySummaryController extends VatRegistrationController with SessionProfile {
  val summaryService: SummaryService
  val vatRegistrationService: VatRegistrationService

  def show: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          summaryService.getEligibilitySummary map (summary => Ok(views.html.pages.summary_eligibility(summary)))
        }
  }

  def submit: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          vatRegistrationService.getIncorporationDate map { date =>
            val redirectLocation = date.fold(routes.TaxableTurnoverController.show())(_ => routes.ThresholdController.goneOverShow())
            Redirect(redirectLocation)
          }
        }
  }
}
