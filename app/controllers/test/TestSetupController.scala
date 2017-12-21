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

package controllers.test

import javax.inject.Inject

import common.enums.CacheKeys._
import connectors.S4LConnector
import controllers.VatRegistrationController
import forms.test.TestSetupForm
import models._
import models.test.{TestSetup, VatEligibilityChoiceTestSetup, VatServiceEligibilityTestSetup}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.CurrentProfileService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.SessionProfile

import scala.concurrent.Future

class TestSetupControllerImpl @Inject()(val s4LConnector: S4LConnector,
                                        val messagesApi: MessagesApi,
                                        val authConnector: AuthConnector,
                                        val currentProfileService: CurrentProfileService,
                                        val s4LBuilder: TestS4LBuilder) extends TestSetupController

trait TestSetupController extends VatRegistrationController with SessionProfile {
  val s4LConnector: S4LConnector
  val s4LBuilder: TestS4LBuilder

  def show: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          for {
            eligibilityChoice <- s4LConnector.fetchAndGet[S4LVatEligibilityChoice](profile.registrationId, EligibilityChoice)
            eligibility <- s4LConnector.fetchAndGet[S4LVatEligibility](profile.registrationId, Eligibility)

            testSetup = TestSetup(
              VatServiceEligibilityTestSetup(
                haveNino = eligibility.flatMap(_.haveNino.map(_.toString)),
                doingBusinessAbroad = eligibility.flatMap(_.doingBusinessAbroad.map(_.toString)),
                doAnyApplyToYou = eligibility.flatMap(_.doAnyApplyToYou.map(_.toString)),
                applyingForAnyOf = eligibility.flatMap(_.applyingForAnyOf.map(_.toString)),
                applyingForVatExemption = eligibility.flatMap(_.applyingForVatExemption.map(_.toString)),
                companyWillDoAnyOf = eligibility.flatMap(_.companyWillDoAnyOf.map(_.toString))
              ),
              VatEligibilityChoiceTestSetup(
                taxableTurnoverChoice = eligibilityChoice.flatMap(_.taxableTurnover.map(_.yesNo)),
                voluntaryChoice = eligibilityChoice.flatMap(_.voluntaryRegistration).map(_.yesNo),
                voluntaryRegistrationReason = eligibilityChoice.flatMap(_.voluntaryRegistrationReason).map(_.reason),
                overThresholdSelection = eligibilityChoice.flatMap(_.overThreshold).map(_.selection.toString),
                overThresholdMonth = eligibilityChoice.flatMap(_.overThreshold).flatMap(_.date).map(_.getMonthValue.toString),
                overThresholdYear = eligibilityChoice.flatMap(_.overThreshold).flatMap(_.date).map(_.getYear.toString),
                expectationOverThresholdSelection = eligibilityChoice.flatMap(_.expectationOverThreshold).map(_.selection.toString),
                expectationOverThresholdDay = eligibilityChoice.flatMap(_.expectationOverThreshold).flatMap(_.date).map(_.getDayOfMonth.toString),
                expectationOverThresholdMonth = eligibilityChoice.flatMap(_.expectationOverThreshold).flatMap(_.date).map(_.getMonthValue.toString),
                expectationOverThresholdYear = eligibilityChoice.flatMap(_.expectationOverThreshold).flatMap(_.date).map(_.getYear.toString)
              )
            )
            form = TestSetupForm.form.fill(testSetup)
          } yield Ok(views.html.pages.test.test_setup(form))
        }
  }

  def submit: Action[AnyContent] = authorised.async {
    implicit user =>
      implicit request =>
        withCurrentProfile { implicit profile =>
          TestSetupForm.form.bindFromRequest().fold(
            badForm => {
              Future.successful(BadRequest(views.html.pages.test.test_setup(badForm)))
            }, {
              data: TestSetup => {
                for {
                  _ <- s4LConnector.save(profile.registrationId, Eligibility, s4LBuilder.eligibilityFromData(data))
                  _ <- s4LConnector.save(profile.registrationId, EligibilityChoice, s4LBuilder.eligibilityChoiceFromData(data))
                } yield Ok("Test setup complete")
              }
            })
        }
  }
}
