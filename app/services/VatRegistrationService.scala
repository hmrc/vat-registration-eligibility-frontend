/*
 * Copyright 2022 HM Revenue & Customs
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

import connectors.VatRegistrationConnector
import featureswitch.core.config.{FeatureSwitching, OBIFlow, TOGCFlow}
import identifiers._
import models.BusinessEntity.businessEntityToString
import models.RegisteringBusiness.registeringBusinessToString
import models.RegistrationReason.registrationReasonToString
import models._
import models.requests.DataRequest
import play.api.i18n.MessagesApi
import play.api.libs.json._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import utils.{JsonSummaryRow, MessagesUtils, PageIdBinding}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VatRegistrationService @Inject()(val vrConnector: VatRegistrationConnector,
                                       val sessionService: SessionService,
                                       val messagesApi: MessagesApi) extends MessagesUtils with FeatureSwitching {

  def submitEligibility(implicit hc: HeaderCarrier, ec: ExecutionContext, request: DataRequest[_]): Future[JsObject] = {
    for {
      block <- createEligibilityBlock
      _ <- vrConnector.saveEligibility(request.regId, block)
    } yield {
      block
    }
  }

  private def createEligibilityBlock(implicit hc: HeaderCarrier,
                                     executionContext: ExecutionContext,
                                     r: DataRequest[_]): Future[JsObject] = {
    sessionService.fetch map {
      case Some(map) => Json.obj("sections" -> getEligibilitySections(map))
      case _ => throw new RuntimeException
    }
  }

  private def getEligibilitySections(cacheMap: CacheMap)(implicit r: DataRequest[_]) =
    PageIdBinding.sectionBindings(cacheMap) map {
      case (sectionTitle, questionIds) => Json.obj(
        "title" -> sectionTitle,
        "data" -> (questionIds flatMap {
          case (questionId, userAnswer) =>
            userAnswer.fold(List[JsValue]())(
              answer => summaryRowBuilder(questionId, answer)
            )
        })
      )
    }

  private[services] def summaryRowBuilder[T](key: Identifier, data: T)(implicit r: DataRequest[_]): List[JsValue] = {
    data match {
      case ConditionalDateFormElement(answer, optDate) =>
        JsonSummaryRow(
          s"$key",
          messageFormatter(key),
          answerFormatter(answer, key),
          answerJsonFormatter(answer)
        ) ++ optDate.toList.flatMap(date =>
          JsonSummaryRow(
            s"$key-optionalData",
            messageFormatter(key, isOptData = true),
            answerFormatter[LocalDate](date, key),
            answerJsonFormatter[LocalDate](date)
          )
        )
      case _ =>
        JsonSummaryRow(
          s"$key",
          messageFormatter(key),
          answerFormatter(data, key),
          answerJsonFormatter(data)
        )
    }
  }

  private def messageFormatter(key: Identifier, isOptData: Boolean = false)(implicit data: DataRequest[_]): String = {
    val optDataKey = if (isOptData) ".optional" else ""
    messages(
      key match {
        case InvolvedInOtherBusinessId =>
          if (isEnabled(TOGCFlow) && isEnabled(OBIFlow)) {
            s"checkYourAnswers.$key.headingVatGroup"
          } else if (isEnabled(TOGCFlow)) {
            s"checkYourAnswers.$key.headingObi"
          } else if (isEnabled(OBIFlow)) {
            s"checkYourAnswers.$key.headingTakingOver"
          } else {
            s"checkYourAnswers.$key"
          }
        case DateOfBusinessTransferId | PreviousBusinessNameId | VATNumberId | KeepOldVrnId | TermsAndConditionsId =>
          data.userAnswers.registrationReason match {
            case Some(TakingOverBusiness) => s"checkYourAnswers.$key$optDataKey.togc"
            case Some(ChangingLegalEntityOfBusiness) => s"checkYourAnswers.$key$optDataKey.cole"
            case _ => throw new InternalServerException("Attempted to submit togc/cole data without a matching reg reason")
          }
        case InternationalActivitiesId | AgriculturalFlatRateSchemeId | RacehorsesId |
             VoluntaryRegistrationId | ThresholdInTwelveMonthsId | ThresholdNextThirtyDaysId |
             ThresholdPreviousThirtyDaysId | RegistrationReasonId =>
          val businessOrPartnership = if (data.userAnswers.isPartnership) ".partnership" else ".business"
          s"checkYourAnswers.$key$optDataKey$businessOrPartnership"
        case _ =>
          s"checkYourAnswers.$key$optDataKey"
      }
    )
  }

  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

  private def answerFormatter[T](answer: T, key: Identifier)(implicit r: DataRequest[_]): String =
    answer match {
      case data: Boolean => messages(s"site.${if (data) "yes" else "no"}")
      case data: String => data
      case data: DateFormElement => data.date.format(formatter)
      case data: LocalDate => data.format(formatter)
      case data: RegistrationReason => registrationReasonToString(data)(messages)
      case data: RegisteringBusiness => registeringBusinessToString(data)(messages)
      case data: BusinessEntity => businessEntityToString(data)(messages)
    }

  private def answerJsonFormatter[T](answer: T): JsValue =
    answer match {
      case data: Boolean => JsBoolean(data)
      case data: String => JsString(data)
      case data: DateFormElement => Json.toJson(data.date)
      case data: LocalDate => JsString(data.format(DateTimeFormatter.ISO_LOCAL_DATE))
      case data: RegistrationReason => Json.toJson(data)(RegistrationReason.writes)
      case data: RegisteringBusiness => Json.toJson(data)(RegisteringBusiness.writes)
      case data: BusinessEntity => Json.toJson(data)(BusinessEntity.writes)
    }

}
