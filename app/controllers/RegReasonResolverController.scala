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
import identifiers.RegReasonResolverId
import models.NormalMode
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.{Navigator, UserAnswers}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RegReasonResolverController @Inject()(navigator: Navigator,
                                            identify: CacheIdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction)
                                           (implicit mcc: MessagesControllerComponents,
                                            appConfig: FrontendAppConfig,
                                            executionContext: ExecutionContext) extends BaseController {

  def resolve: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      Redirect(navigator.nextPage(RegReasonResolverId, NormalMode)(request)(new UserAnswers(request.userAnswers.cacheMap)))
  }
}
