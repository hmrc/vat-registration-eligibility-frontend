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

package filters

import akka.stream.Materializer
import config.FrontendAppConfig
import javax.inject.Inject
import play.api.mvc.Call
import uk.gov.hmrc.whitelist.AkamaiWhitelistFilter

class WhitelistFilter @Inject()(val config: FrontendAppConfig, override val mat: Materializer) extends AkamaiWhitelistFilter {
  override def whitelist: Seq[String]   = config.whitelist
  override def excludedPaths: Seq[Call] = config.whitelistExcluded map(Call("GET", _))
  override def destination: Call        = Call("GET", "https://www.tax.service.gov.uk/outage-register-for-vat")
}
