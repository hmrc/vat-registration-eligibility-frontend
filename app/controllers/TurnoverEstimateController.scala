/*
 * Copyright 2022 HM Revenue & Customs
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
import connectors.SessionService
import controllers.actions._
import forms.TurnoverEstimateFormProvider
import identifiers.{TurnoverEstimateId, ZeroRatedSalesId}

import javax.inject.{Inject, Singleton}
import models.{Mode, TurnoverEstimateFormElement}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.turnoverEstimate

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TurnoverEstimateController @Inject()(mcc: MessagesControllerComponents,
                                           sessionService: SessionService,
                                           navigator: Navigator,
                                           identify: CacheIdentifierAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: TurnoverEstimateFormProvider,
                                           view: turnoverEstimate
                                          )(implicit appConfig: FrontendAppConfig, executionContext: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with Enumerable.Implicits {

  private val ZeroTurnover = TurnoverEstimateFormElement("0")

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.turnoverEstimate match {
        case None => formProvider()
        case Some(value) => formProvider().fill(value)
      }
      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      formProvider().bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          sessionService.save[TurnoverEstimateFormElement](request.internalId, TurnoverEstimateId.toString, value).flatMap { cacheMap =>
            value match {
              case ZeroTurnover =>
                sessionService.save[Boolean](request.internalId, ZeroRatedSalesId.toString, false).map { cacheMap =>
                  Redirect(navigator.nextPage(ZeroRatedSalesId, mode)(new UserAnswers(cacheMap)))
                }
              case _ =>
                Future.successful(Redirect(navigator.nextPage(TurnoverEstimateId, mode)(new UserAnswers(cacheMap))))
            }
          }
      )
  }
}
