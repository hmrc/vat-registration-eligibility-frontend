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

package services

import config.FrontendAppConfig
import models.VatThreshold
import uk.gov.hmrc.http.InternalServerException

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import utils.{CurrencyFormatter, LoggingUtil}


trait ThresholdService  extends LoggingUtil {

  def now: LocalDateTime = LocalDateTime.now()

  def getVatThreshold()(implicit appConfig: FrontendAppConfig): Option[VatThreshold] = {
    appConfig.thresholds
      .sortWith(_.date isAfter _.date)
      .find(model => now.isAfter(model.date) || now.isEqual(model.date))
  }

  def formattedVatThreshold()(implicit appConfig: FrontendAppConfig): String = {
    getVatThreshold().fold {
      val msg = "[ThresholdService][formattedThreshold] Could not retrieve threshold from config"
      logger.error(msg)
      throw new InternalServerException(msg)
    } { threshold =>
      logger.info(s"[ThresholdService][formattedThreshold] displayed threshold ${threshold.amount}")
      CurrencyFormatter.currencyFormat(threshold.amount)
    }
  }
}
