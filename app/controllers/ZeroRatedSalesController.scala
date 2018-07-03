/*
 * Copyright 2018 HM Revenue & Customs
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
import forms.ZeroRatedSalesFormProvider
import identifiers.{VATExemptionId, ZeroRatedSalesId}
import javax.inject.Inject
import models.NormalMode
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import views.html.zeroRatedSales

import scala.concurrent.Future

class ZeroRatedSalesController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         dataCacheConnector: DataCacheConnector,
                                         navigator: Navigator,
                                         identify: CacheIdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: ZeroRatedSalesFormProvider) extends FrontendController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad() = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.zeroRatedSales match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(zeroRatedSales(appConfig, preparedForm, NormalMode))
  }

  def onSubmit() = (identify andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(zeroRatedSales(appConfig, formWithErrors, NormalMode))),
        (value) =>
          dataCacheConnector.save[Boolean](request.internalId, ZeroRatedSalesId.toString, value).flatMap {
            cacheMap =>
              def toRedirectLocation = Redirect(navigator.nextPage(ZeroRatedSalesId, NormalMode)(new UserAnswers(cacheMap)))
              if(value) Future.successful(toRedirectLocation) else dataCacheConnector.remove(cacheMap.id, VATExemptionId.toString).map(_ => toRedirectLocation)
          }
      )
  }
}