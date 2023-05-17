/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.Logging
import play.twirl.api.Html
import views.html.components

import javax.inject.Inject

class OptimizelyHelper @Inject()(div: components.div) extends Viewtils with Logging {

  def render(experimentId: String,
             isEnabled: Boolean,
             control: Html,
             variants: Html*
            ): Html = {

    if (isEnabled) {
      logger.info(s"[OptimizelyHelper][render] - experiment: $experimentId is enabled")
      experimentDiv(
        experimentId = experimentId,
        experimentContent =
          html(
            controlDiv(experimentId, control),
            variantDivs(experimentId, variants: _*)
          )
      )
    } else {
      logger.info(s"[OptimizelyHelper][render] - experiment: $experimentId is disabled")
      experimentDiv(experimentId, control)
    }
  }

  def experimentDiv(experimentId: String, experimentContent: Html): Html = {
    div(
      content = experimentContent,
      id = Some(s"$experimentId")
    )
  }

  def controlDiv(experimentId: String, control: Html): Html = {
    div(
      content = control,
      id = Some(s"$experimentId-control")
    )
  }

  def variantDiv(experimentId: String, index: String, variant: Html): Html = {
    div(
      content = variant,
      id = Some(s"$experimentId-$index"),
      classes = Some("govuk-!-display-none")
    )
  }

  def letterAtIndex(index: Int): String = {
    val charIntOfA = "a"(0)
    (charIntOfA + index).toChar.toString
  }

  def variantDivs(experimentId: String, variants: Html*): Html = {
    html(
      variants.zipWithIndex.map { case (variant, index) =>
        variantDiv(experimentId, letterAtIndex(index), variant)
      }: _*
    )
  }
}
