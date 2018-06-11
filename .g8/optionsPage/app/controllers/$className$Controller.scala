package controllers

import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import connectors.DataCacheConnector
import controllers.actions._
import config.FrontendAppConfig
import forms.$className$FormProvider
import identifiers.$className$Id
import models.{$className$, Mode, NormalMode}
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.$className;format="decap"$

import scala.concurrent.Future

class $className$Controller @Inject()(
                                        appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        dataCacheConnector: DataCacheConnector,
                                        navigator: Navigator,
                                        identify: CacheIdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: $className$FormProvider) extends FrontendController with I18nSupport with Enumerable.Implicits {

  val form = formProvider()

  def onPageLoad() = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.$className;format="decap"$ match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok($className;format="decap"$(appConfig, preparedForm, NormalMode))
  }

  def onSubmit() = (identify andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest($className;format="decap"$(appConfig, formWithErrors, NormalMode))),
        (value) =>
          dataCacheConnector.save[$className$](request.internalId, $className$Id.toString, value).map(cacheMap =>
            Redirect(navigator.nextPage($className$Id, NormalMode)(new UserAnswers(cacheMap))))
      )
  }
}
