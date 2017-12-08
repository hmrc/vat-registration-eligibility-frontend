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

import forms.{ExpectationThresholdForm, OverThresholdFormFactory}
import models.MonthYearModel.FORMAT_DD_MMMM_Y
import models.CurrentProfile
import models.view.{ExpectationOverThresholdView, OverThresholdView}
import play.api.i18n.MessagesApi
import play.api.mvc._
import services._
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.SessionProfile

import scala.concurrent.Future

class ThresholdControllerImpl @Inject()(val messagesApi: MessagesApi,
                                        val authConnector: AuthConnector,
                                        val currentProfileService: CurrentProfileService,
                                        val thresholdService: ThresholdService) extends ThresholdController

trait ThresholdController extends VatRegistrationController with SessionProfile {
  val thresholdService: ThresholdService

  def goneOverShow: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request => {
        withCurrentProfile { implicit profile =>
          hasIncorpDate { date =>
            thresholdService.getThresholdViewModel[OverThresholdView].map {
              view => {
                val incorpDate = date.format(FORMAT_DD_MMMM_Y)
                val form = OverThresholdFormFactory.form(date)
                Ok(views.html.pages.over_threshold(view.fold(form)(form.fill), incorpDate))
              }
            }
          }
        }
      }
  }

  def goneOverSubmit: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          hasIncorpDate { date =>
            OverThresholdFormFactory.form(date).bindFromRequest().fold(
              badForm => Future.successful(BadRequest(views.html.pages.over_threshold(badForm, date.format(FORMAT_DD_MMMM_Y)))),
              data    => thresholdService.saveThreshold(data) map {
                _ => Redirect(controllers.routes.ThresholdController.expectationOverShow())
              }
            )
          }
        }
  }

  def expectationOverShow: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          hasIncorpDate { date =>
            thresholdService.getThresholdViewModel[ExpectationOverThresholdView].map{
              view => {
                val form = ExpectationThresholdForm.form(date)
                Ok(views.html.pages.expectation_over_threshold(view.fold(form)(form.fill)))
              }
            }
          }
        }
  }

  def expectationOverSubmit: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          hasIncorpDate { date =>
            ExpectationThresholdForm.form(date).bindFromRequest().fold(
              badForm => Future.successful(BadRequest(views.html.pages.expectation_over_threshold(badForm))),
              data => thresholdService.saveThreshold(data) map {
                _ => Redirect(controllers.routes.ThresholdSummaryController.show())
              }
            )
          }
        }
  }
}
