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
import controllers.actions.{CacheIdentifierAction, DataRequiredAction, DataRetrievalAction}
import featureswitch.core.config.FeatureSwitching
import forms.GoneOverThresholdFormProvider
import identifiers.GoneOverThresholdId
import models.NormalMode
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.S4LService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.{Navigator, UserAnswers}
import views.html.GoneOverThreshold

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GoneOverThresholdController @Inject()(mcc: MessagesControllerComponents,
                                            dataCacheConnector: DataCacheConnector,
                                            s4LService: S4LService,
                                            navigator: Navigator,
                                            identify: CacheIdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            formProvider: GoneOverThresholdFormProvider,
                                            view: GoneOverThreshold
                                           )(implicit appConfig: FrontendAppConfig,
                                             executionContext: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with FeatureSwitching {

  val onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.goneOverThreshold match {
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
          dataCacheConnector.save[Boolean](request.internalId, GoneOverThresholdId.toString, value).flatMap {
            cacheMap => Future.successful(cacheMap)
          }.map(cMap => Redirect(navigator.nextPage(GoneOverThresholdId, NormalMode)(new UserAnswers(cMap)))))
  }
}
