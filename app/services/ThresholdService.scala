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

package services

import config.FrontendAppConfig
import connectors.DataCacheConnector
import deprecated.DeprecatedConstants
import identifiers.{ThresholdNextThirtyDaysId, VATRegistrationExceptionId, VoluntaryRegistrationId}
import javax.inject.Inject
import models.requests.DataRequest
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.VATDateHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ThresholdServiceImpl @Inject()(val dataCacheConnector: DataCacheConnector,
                                     val messagesApi: MessagesApi,
                                     val appConfig: FrontendAppConfig) extends ThresholdService

trait ThresholdService extends I18nSupport {
  val dataCacheConnector: DataCacheConnector
  val messagesApi: MessagesApi
  val appConfig: FrontendAppConfig


  def removeVoluntaryAndNextThirtyDays(implicit request: DataRequest[_]): Future[CacheMap] = {
    dataCacheConnector.removeEntry(request.internalId, VoluntaryRegistrationId.toString).flatMap {
      _ => dataCacheConnector.removeEntry(request.internalId, ThresholdNextThirtyDaysId.toString)
    }
  }

  def removeException(implicit request: DataRequest[_]): Future[CacheMap] = {
    dataCacheConnector.removeEntry(request.internalId, VATRegistrationExceptionId.toString)
  }

  def removeVoluntaryRegistration(implicit request: DataRequest[_]): Future[CacheMap] = {
    dataCacheConnector.removeEntry(request.internalId, VoluntaryRegistrationId.toString)
  }

  sealed trait ThresholdDateResult

  case class notIncorped() extends ThresholdDateResult

  case class limitedIncorpedLessThan12MonthsAgo() extends ThresholdDateResult

  case class limitedIncorpedEqualOrAfter20170401() extends ThresholdDateResult

  case class limitedIncorpedTaxYear2016to2017() extends ThresholdDateResult

  case class limitedIncorpedTaxYear2015to2016() extends ThresholdDateResult

  case class default() extends ThresholdDateResult


  def returnHeadingTwelveMonths(enum: ThresholdDateResult)(implicit r: DataRequest[_]): String = {
    enum match {
      case notIncorped() => messagesApi("threshold.headingNotIncorp")
      case limitedIncorpedLessThan12MonthsAgo() => messagesApi("thresholdInTwelveMonths.headingIncorpLess12m")
      case _ => messagesApi("thresholdInTwelveMonths.headingIncorpMore12m")
    }
  }

  def returnHeadingForTwelveMonthsDateEntry(enum: ThresholdDateResult)(implicit r: DataRequest[_]): String = {
    enum match {
      case _ => messagesApi("thresholdInTwelveMonths.heading2")
    }
  }

  def returnHelpText1TwelveMonths(enum: ThresholdDateResult)(implicit r: DataRequest[_], messages: Messages): String = {
    enum match {
      case notIncorped() => messages("thresholdInTwelveMonths.firstHelpTextNotIncorp")
      case limitedIncorpedLessThan12MonthsAgo() => messages("thresholdInTwelveMonths.firstHelpTextIncorpLess12m")
      case _ => messages("thresholdInTwelveMonths.firstHelpTextIncorpMore12m")
    }
  }


  def returnHeadingPrevious(enum: ThresholdDateResult)(implicit r: DataRequest[_]): String = {
    enum match {
      case (limitedIncorpedEqualOrAfter20170401() | limitedIncorpedLessThan12MonthsAgo()) => messagesApi("thresholdPreviousThirtyDays.headingLtdAndIncorpAfterApr17")
      case _ => messagesApi("thresholdPreviousThirtyDays.heading")
    }
  }

  def returnHelpText1Previous(enum: ThresholdDateResult)(implicit r: DataRequest[_]): Html = {
    enum match {
      case (limitedIncorpedEqualOrAfter20170401() | limitedIncorpedLessThan12MonthsAgo()) => Html("")
      case limitedIncorpedTaxYear2016to2017() => HtmlFormat.fill(collection.immutable.Seq(
        views.html.newcomponents.p {
          Html(messagesApi("thresholdPreviousThirtyDays.text"))
        },
        views.html.newcomponents.bullets(
          messagesApi("thresholdPreviousThirtyDays.bullet1"),
          messagesApi("thresholdPreviousThirtyDays.bullet2")
        )
      ))
      case limitedIncorpedTaxYear2015to2016() => HtmlFormat.fill(collection.immutable.Seq(
        views.html.newcomponents.p {
          Html(messagesApi("thresholdPreviousThirtyDays.text"))
        },
        views.html.newcomponents.bullets(
          messagesApi("thresholdPreviousThirtyDays.bullet1"),
          messagesApi("thresholdPreviousThirtyDays.bullet2"),
          messagesApi("thresholdPreviousThirtyDays.bullet3")
        )
      ))
      case _ => HtmlFormat.fill(collection.immutable.Seq(
        views.html.newcomponents.p {
          Html(messagesApi("thresholdPreviousThirtyDays.text"))
        },
        views.html.newcomponents.bullets(
          messagesApi("thresholdPreviousThirtyDays.bullet1"),
          messagesApi("thresholdPreviousThirtyDays.bullet2"),
          messagesApi("thresholdPreviousThirtyDays.bullet3")
        ),
        views.html.newcomponents.p {
          Html(messagesApi("thresholdPreviousThirtyDays.beforeLinkText"))
          views.html.newcomponents.link(appConfig.VATNotice700_1supplementURL, messagesApi("thresholdPreviousThirtyDays.linkText"))
          Html(".")
        }
      )
      )
    }
  }

  def returnThresholdDateResult[T](f: ThresholdDateResult => T)(implicit request: DataRequest[_]): T = {
    val res = Option(DeprecatedConstants.fakeIncorpDate) match { //TODO - Option to allow the match to compile, but will never hit the none route
      case None => notIncorped() //TODO - Is this needed still?
      case Some(d) if VATDateHelper.lessThan12Months(d) => limitedIncorpedLessThan12MonthsAgo()
      case Some(d) if VATDateHelper.dateEqualOrAfter201741(d) => limitedIncorpedEqualOrAfter20170401()
      case Some(d) if VATDateHelper.dateBefore201741After2016331(d) => limitedIncorpedTaxYear2016to2017()
      case Some(d) if VATDateHelper.dateBefore201641After2015331(d) => limitedIncorpedTaxYear2015to2016()
      case _ => default()
    }
    f(res)
  }
}