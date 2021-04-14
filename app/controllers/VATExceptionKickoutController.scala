/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.VATExceptionKickoutFormProvider
import identifiers.VATExceptionKickoutId
import javax.inject.{Inject, Singleton}
import models.{NormalMode, RegistrationInformation}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.TrafficManagementService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import views.html.vatExceptionKickout

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VATExceptionKickoutController @Inject()(mcc: MessagesControllerComponents,
                                              dataCacheConnector: DataCacheConnector,
                                              navigator: Navigator,
                                              identify: CacheIdentifierAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              formProvider: VATExceptionKickoutFormProvider,
                                              trafficManagementService: TrafficManagementService,
                                              view: vatExceptionKickout
                                             )(implicit appConfig: FrontendAppConfig, executionContext: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.vatExceptionKickout match {
        case None => formProvider()
        case Some(value) => formProvider().fill(value)
      }
      Ok(view(preparedForm, NormalMode))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      formProvider().bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, NormalMode))),
        value =>
          dataCacheConnector.save[Boolean](request.internalId, VATExceptionKickoutId.toString, value).flatMap(cacheMap =>
            trafficManagementService.upsertRegistrationInformation(request.internalId, request.currentProfile.registrationID, isOtrs = true).map {
              case RegistrationInformation(_, _, _, _, _) =>
                Redirect(navigator.nextPage(VATExceptionKickoutId, NormalMode)(new UserAnswers(cacheMap)))
            }
          )
      )
  }
}
