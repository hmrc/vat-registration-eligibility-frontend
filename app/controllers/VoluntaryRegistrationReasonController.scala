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

import forms.VoluntaryRegistrationReasonForm
import models.view.VoluntaryRegistrationReason
import play.api.i18n.MessagesApi
import play.api.mvc._
import services.{CurrentProfileService, ThresholdService, VatRegFrontendService}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.SessionProfile

import scala.concurrent.Future

class VoluntaryRegistrationReasonControllerImpl @Inject()(val messagesApi: MessagesApi,
                                                          val authConnector: AuthConnector,
                                                          val currentProfileService: CurrentProfileService,
                                                          val vatRegFrontendService: VatRegFrontendService,
                                                          val thresholdService: ThresholdService) extends VoluntaryRegistrationReasonController

trait VoluntaryRegistrationReasonController extends VatRegistrationController with SessionProfile  {
  val thresholdService: ThresholdService
  val vatRegFrontendService: VatRegFrontendService

  val form = VoluntaryRegistrationReasonForm.form

  def show: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          thresholdService.getThresholdViewModel[VoluntaryRegistrationReason].map { view =>
            Ok(views.html.pages.voluntary_registration_reason(view.fold(form)(form.fill)))
          }
        }
  }

  def submit: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          form.bindFromRequest().fold(
            badForm => Future.successful(BadRequest(views.html.pages.voluntary_registration_reason(badForm))),
            data    => thresholdService.saveThreshold(data) map { _ =>
              if (data.reason == VoluntaryRegistrationReason.NEITHER) {
                Redirect(vatRegFrontendService.buildVatRegFrontendUrlWelcome)
              } else {
                Redirect(vatRegFrontendService.buildVatRegFrontendUrlEntry)
              }
            }
          )
        }
  }
}
