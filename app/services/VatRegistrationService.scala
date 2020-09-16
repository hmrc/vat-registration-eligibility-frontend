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

import java.time.format.DateTimeFormatter

import connectors.{DataCacheConnector, VatRegistrationConnector}
import deprecated.DeprecatedConstants
import identifiers._
import javax.inject.Inject
import models._
import models.requests.DataRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{JsonSummaryRow, PageIdBinding}

import scala.concurrent.{ExecutionContext, Future}

class VatRegistrationServiceImpl @Inject()(val vrConnector: VatRegistrationConnector,
                                           val dataCacheConnector: DataCacheConnector,
                                           val messagesApi: MessagesApi,
                                           val thresholdService: ThresholdService) extends VatRegistrationService {
}

trait VatRegistrationService extends I18nSupport {
  val vrConnector: VatRegistrationConnector
  val dataCacheConnector: DataCacheConnector
  val thresholdService: ThresholdService
  implicit lazy val messages = messagesApi

  def submitEligibility(internalId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext, r: DataRequest[_]): Future[JsObject] = {
    for {
      block <- createEligibilityBlock(internalId, Nil) //TODO - officers was being passed down here but is no longer available
      _ <- vrConnector.saveEligibility(r.currentProfile.registrationID, block)
    } yield {
      block
    }
  }

  val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

  private[services] def prepareQuestionData(key: String, data: Boolean)(implicit r: DataRequest[_]): List[JsValue] = {
    JsonSummaryRow(key, messagesApi(s"$key.heading", DeprecatedConstants.fakeCompanyName), messagesApi(s"site.${if (data) "yes" else "no"}"), Json.toJson(data))
  }

  private[services] def getVoluntaryRegistrationJson(data: Boolean)(implicit r: DataRequest[_]): List[JsValue] = {
    val key = VoluntaryRegistrationId.toString
    JsonSummaryRow(key, messagesApi(s"$key.summary.heading"), messagesApi(s"site.${if (data) "yes" else "no"}"), Json.toJson(data))
  }

  private[services] def prepareQuestionData(key: String, data: String): List[JsValue] = {
    JsonSummaryRow(key, messagesApi(s"$key.heading"), data, Json.toJson(data))
  }

  private[services] def prepareQuestionData(key: String, data: ConditionalDateFormElement)()(implicit r: DataRequest[_]): List[JsValue] = {
    val value = JsonSummaryRow(s"$key-value", messagesApi(s"$key.heading", DeprecatedConstants.fakeCompanyName), messagesApi(if (data.value) s"site.yes" else "site.no"), Json.toJson(data.value))
    val dataObj = data.optionalData.map(date => JsonSummaryRow(s"$key-optionalData", messagesApi(s"$key.heading2", DeprecatedConstants.fakeCompanyName), date.format(formatter), Json.toJson(date)))

    dataObj.foldLeft(value)((old, list) => old ++ list)
  }

  private[services] def prepareThresholdInTwelveMonths(key: String, data: ConditionalDateFormElement)()(implicit r: DataRequest[_]): List[JsValue] = {
    val value = JsonSummaryRow(s"$key-value", thresholdService.returnThresholdDateResult[String](thresholdService.returnHeadingTwelveMonths), messagesApi(if (data.value) s"site.yes" else "site.no"), Json.toJson(data.value))
    val dataObj = data.optionalData.map(date => JsonSummaryRow(s"$key-optionalData", thresholdService.returnThresholdDateResult[String](thresholdService.returnHeadingForTwelveMonthsDateEntry), date.format(formatter), Json.toJson(date)))

    dataObj.foldLeft(value)((old, list) => old ++ list)
  }

  private[services] def prepareThresholdPreviousThirty(key: String, data: ConditionalDateFormElement)()(implicit r: DataRequest[_]): List[JsValue] = {
    val value = JsonSummaryRow(s"$key-value", thresholdService.returnThresholdDateResult[String](thresholdService.returnHeadingPrevious), messagesApi(if (data.value) s"site.yes" else "site.no"), Json.toJson(data.value))
    val dataObj = data.optionalData.map(date => JsonSummaryRow(s"$key-optionalData", messagesApi(s"$key.heading2"), date.format(formatter), Json.toJson(date)))

    dataObj.foldLeft(value)((old, list) => old ++ list)
  }

  private[services] def prepareDateData(key: String, data: ConditionalDateFormElement)()(implicit r: DataRequest[_]): List[JsValue] = {
    val value = JsonSummaryRow(s"$key-value", messagesApi(s"$key.heading"), messagesApi(if (data.value) s"site.yes" else "site.no"), Json.toJson(data.value))
    val dataObj = data.optionalData.map(date => JsonSummaryRow(s"$key-optionalData", messagesApi(s"$key.heading2"), date.format(formatter), Json.toJson(date)))

    dataObj.foldLeft(value)((old, list) => old ++ list)
  }

  private[services] def prepareQuestionData(key: String, data: TurnoverEstimateFormElement)(implicit r: DataRequest[_]): List[JsValue] = {
    JsonSummaryRow(s"$key-value", messagesApi(s"$key.heading"), s"£${"%,d".format(data.value.toLong)}", JsNumber(BigDecimal(data.value.toLong)))
  }

  private[services] def prepareQuestionData(key: String, data: ConditionalNinoFormElement, officers: Seq[Officer], onBehalfOf: Option[String]): List[JsValue] = {
    val heading = onBehalfOf.fold(messagesApi(s"$key.heading")) {
      id =>
        val officer = officers.find(_.generateId == id).getOrElse(throw new Exception("director not present"))
        messagesApi(s"$key.heading.onBehalfOf", officer.shortName)
    }
    val value = JsonSummaryRow(s"$key-value", heading, messagesApi(if (data.value) s"site.yes" else "site.no"), Json.toJson(data.value))
    val dataObj = data.optionalData.map(nino => JsonSummaryRow(s"$key-optionalData", messagesApi(s"$key.heading2"), nino, Json.toJson(nino)))

    dataObj.foldLeft(value)((oList, dList) => oList ++ dList)
  }

  private[services] def buildIndividualQuestion(officers: Seq[Officer], onBehalfOf: Option[String])(implicit r: DataRequest[_]): PartialFunction[(Identifier, Any), List[JsValue]] = {
    case (id@ThresholdInTwelveMonthsId, e: ConditionalDateFormElement) => prepareThresholdInTwelveMonths(id.toString, e)
    case (id@ThresholdNextThirtyDaysId, e: ConditionalDateFormElement) => prepareDateData(id.toString, e)
    case (id@ThresholdPreviousThirtyDaysId, e: ConditionalDateFormElement) => prepareThresholdPreviousThirty(id.toString, e)
    case (id, e: ConditionalDateFormElement) => prepareQuestionData(id.toString, e)
    case (id, e: ConditionalNinoFormElement) => prepareQuestionData(id.toString, e, officers, onBehalfOf)
    case (id, e: TurnoverEstimateFormElement) => prepareQuestionData(id.toString, e)
    case (VoluntaryRegistrationId, e: Boolean) => getVoluntaryRegistrationJson(e)
    case (id, e: Boolean) => prepareQuestionData(id.toString, e)
    case (id, e: String) => prepareQuestionData(id.toString, e)
  }

  private def getEligibilitySections(cacheMap: CacheMap, officers: Seq[Officer])(implicit r: DataRequest[_]) =
    PageIdBinding.sectionBindings(cacheMap) map {
      case (sectionTitle, questionIds) => Json.obj(
        "title" -> sectionTitle,
        "data" -> (questionIds flatMap {
          case (questionId, userAnswer) =>
            userAnswer.fold(List[JsValue]())(
              answer => buildIndividualQuestion(officers, Some(DeprecatedConstants.fakeOfficerName))(r)((questionId, answer))
            )
        })
      )
    }

  private def createEligibilityBlock(internalId: String, officer: Seq[Officer])(implicit hc: HeaderCarrier, executionContext: ExecutionContext, r: DataRequest[_]): Future[JsObject] = {
    dataCacheConnector.fetch(internalId) map {
      case Some(map) => Json.obj("sections" -> getEligibilitySections(map, officer))
      case _ => throw new RuntimeException
    }
  }
}