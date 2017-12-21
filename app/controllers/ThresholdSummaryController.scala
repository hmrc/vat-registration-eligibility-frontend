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

import controllers.builders.SummaryVatThresholdBuilder
import models.CurrentProfile
import models.MonthYearModel.FORMAT_DD_MMMM_Y
import models.view.{Summary, Threshold}
import play.api.i18n.MessagesApi
import play.api.mvc._
import services._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.SessionProfile

import scala.concurrent.Future

class ThresholdSummaryControllerImpl @Inject()(val messagesApi: MessagesApi,
                                               val authConnector: AuthConnector,
                                               val vrs: VatRegistrationService,
                                               val currentProfileService: CurrentProfileService,
                                               val vatRegFrontendService: VatRegFrontendService,
                                               val thresholdService: ThresholdService) extends ThresholdSummaryController

trait ThresholdSummaryController extends VatRegistrationController with SessionProfile {
  val thresholdService: ThresholdService
  val vatRegFrontendService: VatRegFrontendService

  def show: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          hasIncorpDate { incorpDate =>
            getThresholdSummary map { thresholdSummary =>
              Ok(views.html.pages.threshold_summary(thresholdSummary, incorpDate.format(FORMAT_DD_MMMM_Y)))
            }
          }
        }
  }

  def submit: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request => {
        withCurrentProfile { implicit profile =>
          thresholdService.getThreshold map { t =>
            if(t.overThreshold.exists(_.selection) || t.expectationOverThreshold.exists(_.selection)) {
              Redirect(vatRegFrontendService.buildVatRegFrontendUrlEntry)
            } else {
              Redirect(controllers.routes.VoluntaryRegistrationController.show())
            }
          }
        }
      }
  }

  def getThresholdSummary(implicit hc: HeaderCarrier, profile: CurrentProfile): Future[Summary] =
    thresholdService.getThreshold.map(a => thresholdToSummary(a))

  def thresholdToSummary(t:Threshold): Summary = {
    Summary(Seq(SummaryVatThresholdBuilder(t).section))
  }
}
