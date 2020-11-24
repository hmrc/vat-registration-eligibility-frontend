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
import forms.TurnoverEstimateFormProvider
import identifiers.TurnoverEstimateId
import javax.inject.{Inject, Singleton}
import models.{Mode, TurnoverEstimateFormElement}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.turnoverEstimate

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TurnoverEstimateController @Inject()(mcc: MessagesControllerComponents,
                                           dataCacheConnector: DataCacheConnector,
                                           navigator: Navigator,
                                           identify: CacheIdentifierAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: TurnoverEstimateFormProvider
                                          )(implicit appConfig: FrontendAppConfig, executionContext: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with Enumerable.Implicits {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.turnoverEstimate match {
        case None => formProvider()
        case Some(value) => formProvider().fill(value)
      }
      Ok(turnoverEstimate(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      formProvider().bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(turnoverEstimate(formWithErrors, mode))),
        value =>
          dataCacheConnector.save[TurnoverEstimateFormElement](request.internalId, TurnoverEstimateId.toString, value).map(cacheMap =>
            Redirect(navigator.nextPage(TurnoverEstimateId, mode)(new UserAnswers(cacheMap))))
      )
  }
}
