/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.actions._
import forms.TaxableSuppliesInUkFormProvider
import identifiers.TaxableSuppliesInUkId
import models.NormalMode
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import utils.{Navigator, UserAnswers}
import views.html.TaxableSuppliesInUk

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaxableSuppliesInUkController @Inject()(sessionService: SessionService,
                                              navigator: Navigator,
                                              identify: CacheIdentifierAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              formProvider: TaxableSuppliesInUkFormProvider,
                                              view: TaxableSuppliesInUk)
                                             (implicit appConfig: FrontendAppConfig,
                                              mcc: MessagesControllerComponents,
                                              executionContext: ExecutionContext) extends BaseController {

  val onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.taxableSuppliesInUk match {
        case None => formProvider()
        case Some(value) => formProvider().fill(value)
      }

      Ok(view(preparedForm, NormalMode))
  }

  val onSubmit: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      formProvider().bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, NormalMode))),
        value =>
          sessionService.save[Boolean](TaxableSuppliesInUkId.toString, value).map { cacheMap =>
            Redirect(navigator.nextPage(TaxableSuppliesInUkId, NormalMode)(request)(new UserAnswers(cacheMap)))
          }
      )
  }

}
