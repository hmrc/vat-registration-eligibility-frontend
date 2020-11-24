/*
 * Copyright 2020 HM Revenue & Customs
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

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.actions._
import forms.ThresholdNextThirtyDaysFormProvider
import identifiers.ThresholdNextThirtyDaysId
import javax.inject.{Inject, Singleton}
import models.{ConditionalDateFormElement, NormalMode}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ThresholdService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import views.html.thresholdNextThirtyDays

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ThresholdNextThirtyDaysController @Inject()(mcc: MessagesControllerComponents,
                                                  dataCacheConnector: DataCacheConnector,
                                                  navigator: Navigator,
                                                  identify: CacheIdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  thresholdService: ThresholdService,
                                                  formProvider: ThresholdNextThirtyDaysFormProvider
                                                 )(implicit appConfig: FrontendAppConfig, executionContext: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.thresholdNextThirtyDays match {
        case None => formProvider()
        case Some(value) => formProvider().fill(value)
      }
      Ok(thresholdNextThirtyDays(preparedForm, NormalMode))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      formProvider().bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(thresholdNextThirtyDays(formWithErrors, NormalMode))),
        formValue =>
          dataCacheConnector.save[ConditionalDateFormElement](request.internalId, ThresholdNextThirtyDaysId.toString, formValue).flatMap {
            cacheMap =>
              if (formValue.value) {
                thresholdService.removeVoluntaryRegistration
              } else {
                Future.successful(cacheMap)
              }
          }.map(cMap => Redirect(navigator.nextPage(ThresholdNextThirtyDaysId, NormalMode)(new UserAnswers(cMap)))))
  }
}