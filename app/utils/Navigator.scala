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

package utils

import javax.inject.{Inject, Singleton}

import play.api.mvc.Call
import controllers.routes
import identifiers._
import models.Mode

@Singleton
class Navigator @Inject()() {

  private[utils] def pageIdToPageLoad(pageId: Identifier): Call = pageId match {
    case _ => throw new RuntimeException(s"[Navigator] [pageIdToPageLoad] Could not load page for pageId: $pageId")
  }

//  private def ineligiblePage(pageId: Identifier) = routes.IneligibleController.onPageLoad(pageId.toString)

  private[utils] def nextOnFalse(fromPage: Identifier, toPage: Identifier): (Identifier, UserAnswers => Call) = {
    fromPage -> {
      _.getAnswer(fromPage) match {
        case Some(false) => pageIdToPageLoad(toPage)
        case _ => pageIdToPageLoad(toPage)
//        case _ => ineligiblePage(fromPage)
      }
    }
  }

  private val routeMap: Map[Identifier, UserAnswers => Call] = Map(
  )

  def nextPage(id: Identifier, mode: Mode): UserAnswers => Call =
    routeMap.getOrElse(id, _ => routes.IndexController.onPageLoad())
}
