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
import controllers.actions.{CacheIdentifierAction, DataRequiredAction, DataRetrievalAction}
import featureswitch.core.models.FeatureSwitch
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.VatRegistrationService
import views.html.MtdInformation

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MtdInformationController @Inject()(identify: CacheIdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         view: MtdInformation,
                                         vatRegistrationService: VatRegistrationService)
                                        (implicit appConfig: FrontendAppConfig,
                                         mcc: MessagesControllerComponents,
                                         executionContext: ExecutionContext) extends BaseController {

  def onPageLoad: Action[AnyContent] = identify { implicit request =>
    Ok(view())
  }

  def onSubmit: Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    if (isEnabled(FeatureSwitch.SubmitDeadlinePage)) {
      Future.successful(Redirect(routes.SubmitDeadlineController.onPageLoad))
    } else {
    vatRegistrationService.submitEligibility(hc, implicitly[ExecutionContext], request).map { _ =>
      Redirect(s"${appConfig.vatRegFEURL}${appConfig.vatRegFEURI}/journey/${request.regId}")
    }
    }
  }

}
