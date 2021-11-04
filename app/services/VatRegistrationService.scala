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

package services

import java.time.format.DateTimeFormatter
import connectors.{DataCacheConnector, VatRegistrationConnector}
import identifiers._

import javax.inject.{Inject, Singleton}
import models.BusinessEntity.businessEntityToString
import models.RegisteringBusiness.registeringBusinessToString
import models.RegistrationReason.registrationReasonToString
import models._
import models.requests.DataRequest
import play.api.i18n.MessagesApi
import play.api.libs.json._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{JsonSummaryRow, MessagesUtils, PageIdBinding}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VatRegistrationService @Inject()(val vrConnector: VatRegistrationConnector,
                                       val dataCacheConnector: DataCacheConnector,
                                       val messagesApi: MessagesApi) extends MessagesUtils {

  def submitEligibility(internalId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext, r: DataRequest[_]): Future[JsObject] = {
    for {
      block <- createEligibilityBlock(internalId)
      _ <- vrConnector.saveEligibility(r.currentProfile.registrationID, block)
    } yield {
      block
    }
  }

  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

  private[services] def prepareQuestionData(key: String, data: Boolean)(implicit r: DataRequest[_]): List[JsValue] = {
    JsonSummaryRow(key, messages(s"$key.heading"), messages(s"site.${if (data) "yes" else "no"}"), Json.toJson(data))
  }

  private[services] def getVoluntaryRegistrationJson(data: Boolean)(implicit r: DataRequest[_]): List[JsValue] = {
    val key = VoluntaryRegistrationId.toString
    JsonSummaryRow(key, messages(s"$key.heading"), messages(s"site.${if (data) "yes" else "no"}"), Json.toJson(data))
  }

  private[services] def getVoluntaryInformationJson(data: Boolean)(implicit r: DataRequest[_]): List[JsValue] = {
    val key = VoluntaryInformationId.toString
    JsonSummaryRow(key, messages(s"$key.heading"), messages(s"site.${if (data) "yes" else "no"}"), Json.toJson(data))
  }

  private[services] def prepareQuestionData(key: String, data: String)(implicit r: DataRequest[_]): List[JsValue] = {
    JsonSummaryRow(key, messages(s"$key.heading"), data, Json.toJson(data))
  }

  private[services] def prepareQuestionData(key: String, data: ConditionalDateFormElement)(implicit r: DataRequest[_]): List[JsValue] = {
    val value = JsonSummaryRow(s"$key-value", messages(s"$key.heading"), messages(if (data.value) s"site.yes" else "site.no"), Json.toJson(data.value))
    val dataObj = data.optionalData.map(date => JsonSummaryRow(s"$key-optionalData", messages(s"$key.heading2"), date.format(formatter), Json.toJson(date)))

    dataObj.foldLeft(value)((old, list) => old ++ list)
  }

  private[services] def prepareThresholdInTwelveMonths(key: String, data: ConditionalDateFormElement)
                                                      (implicit r: DataRequest[_]): List[JsValue] = {
    val value = JsonSummaryRow(
      questionId = s"$key-value",
      question = messages("thresholdInTwelveMonths.headingIncorpMore12m"),
      answer = messages(if (data.value) s"site.yes" else "site.no"),
      answerValue = Json.toJson(data.value)
    )

    val dataObj = data.optionalData.map(date =>
      JsonSummaryRow(
        s"$key-optionalData",
        messages("thresholdInTwelveMonths.heading2"),
        date.format(formatter),
        Json.toJson(date)
      )
    )

    dataObj.foldLeft(value)((old, list) => old ++ list)
  }

  private[services] def prepareThresholdPreviousThirty(key: String, data: ConditionalDateFormElement)(implicit r: DataRequest[_]): List[JsValue] = {
    val value = JsonSummaryRow(s"$key-value", messages("thresholdPreviousThirtyDays.heading"), messages(if (data.value) s"site.yes" else "site.no"), Json.toJson(data.value))
    val dataObj = data.optionalData.map(date => JsonSummaryRow(s"$key-optionalData", messages(s"$key.heading2"), date.format(formatter), Json.toJson(date)))

    dataObj.foldLeft(value)((old, list) => old ++ list)
  }

  private[services] def prepareBusinessEntity(key: String, data: BusinessEntity)(implicit r: DataRequest[_]): List[JsValue] = {
    JsonSummaryRow(s"$key-value", messages(s"$key.heading"), businessEntityToString(data)(messages), Json.toJson(data))
  }

  private[services] def prepareConditionalDateData(key: String, data: ConditionalDateFormElement)(implicit r: DataRequest[_]): List[JsValue] = {
    val value = JsonSummaryRow(s"$key-value", messages(s"$key.heading"), messages(if (data.value) s"site.yes" else "site.no"), Json.toJson(data.value))
    val dataObj = data.optionalData.map(date => JsonSummaryRow(s"$key-optionalData", messages(s"$key.heading2"), date.format(formatter), Json.toJson(date)))

    dataObj.foldLeft(value)((old, list) => old ++ list)
  }

  private[services] def prepareDateData(key: String, data: DateFormElement)(implicit r: DataRequest[_]): List[JsValue] = {
    JsonSummaryRow(s"$key-value", messages(s"$key.heading"), data.date.format(formatter), Json.toJson(data.date))
  }

  private[services] def prepareQuestionData(key: String, data: TurnoverEstimateFormElement)(implicit r: DataRequest[_]): List[JsValue] = {
    JsonSummaryRow(s"$key-value", messages(s"$key.heading"), s"£${"%,d".format(data.value.toLong)}", JsNumber(BigDecimal(data.value.toLong)))
  }

  private[services] def prepareRegistrationReasonData(key: String, data: RegistrationReason)(implicit r: DataRequest[_]): List[JsValue] = {
    JsonSummaryRow(s"$key-value", messages(s"$key.heading"), registrationReasonToString(data)(messages), Json.toJson(data))
  }

  private[services] def prepareRegisteringBusinessData(key: String, data: RegisteringBusiness)(implicit r: DataRequest[_]): List[JsValue] = {
    JsonSummaryRow(s"$key-value", messages(s"$key.heading"), registeringBusinessToString(data)(messages), Json.toJson(data))
  }

  private[services] def buildIndividualQuestion(implicit r: DataRequest[_]): PartialFunction[(Identifier, Any), List[JsValue]] = {
    case (id@BusinessEntityId, e: BusinessEntity) => prepareBusinessEntity(id.toString, e)
    case (id@ThresholdInTwelveMonthsId, e: ConditionalDateFormElement) => prepareThresholdInTwelveMonths(id.toString, e)
    case (id@ThresholdNextThirtyDaysId, e: ConditionalDateFormElement) => prepareConditionalDateData(id.toString, e)
    case (id@ThresholdPreviousThirtyDaysId, e: ConditionalDateFormElement) => prepareThresholdPreviousThirty(id.toString, e)
    case (id@ThresholdTaxableSuppliesId, e: DateFormElement) => prepareDateData(id.toString, e)
    case (id@RegistrationReasonId, e: RegistrationReason) => prepareRegistrationReasonData(id.toString, e)
    case (id@RegisteringBusinessId, e: RegisteringBusiness) => prepareRegisteringBusinessData(id.toString, e)
    case (id, e: ConditionalDateFormElement) => prepareQuestionData(id.toString, e)
    case (id, e: TurnoverEstimateFormElement) => prepareQuestionData(id.toString, e)
    case (VoluntaryRegistrationId, e: Boolean) => getVoluntaryRegistrationJson(e)
    case (VoluntaryInformationId, e: Boolean) => getVoluntaryInformationJson(e)
    case (id, e: Boolean) => prepareQuestionData(id.toString, e)
    case (id, e: String) => prepareQuestionData(id.toString, e)
  }

  private def getEligibilitySections(cacheMap: CacheMap)(implicit r: DataRequest[_]) =
    PageIdBinding.sectionBindings(cacheMap) map {
      case (sectionTitle, questionIds) => Json.obj(
        "title" -> sectionTitle,
        "data" -> (questionIds flatMap {
          case (questionId, userAnswer) =>
            userAnswer.fold(List[JsValue]())(
              answer => buildIndividualQuestion(r)((questionId, answer))
            )
        })
      )
    }

  private def createEligibilityBlock(internalId: String)
                                    (implicit hc: HeaderCarrier,
                                     executionContext: ExecutionContext,
                                     r: DataRequest[_]): Future[JsObject] = {
    dataCacheConnector.fetch(internalId) map {
      case Some(map) => Json.obj("sections" -> getEligibilitySections(map))
      case _ => throw new RuntimeException
    }
  }
}
