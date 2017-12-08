/*
 * Copyright 2017 HM Revenue & Customs
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

import scala.concurrent.duration._
import akka.util.Timeout
import helpers.VatRegSpec
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentType, headers, redirectLocation}

class SessionControllerSpec extends VatRegSpec {

  val testController = new SessionController {
    override protected def authConnector = mockAuthConnector
    override def messagesApi = mockMessages
  }

  implicit val duration: Timeout = 5.seconds

  //TODO: This should work
  "renewSession" should {
    "return 200 when hit with Authorised User" ignore {
      callAuthorised(testController.renewSession()){ a =>
        redirectLocation(a) shouldBe ""
        status(a) shouldBe 200
        contentType(a) shouldBe Some("image/jpeg")
        await(a).body.dataStream.toString.contains("""renewSession.jpg""")  shouldBe true
      }
    }
  }

  "destroySession" should {
    "return redirect to timeout show and get rid of headers" in {

      val fr = FakeRequest().withHeaders(("playFoo","no more"))

      val res = testController.destroySession()(fr)
      status(res) shouldBe 303
      headers(res).contains("playFoo") shouldBe false

      redirectLocation(res) shouldBe Some(controllers.routes.SessionController.timeoutShow().url)
    }
  }

  "timeoutShow" should {
    "return 200" in {
      val res = testController.timeoutShow()(FakeRequest())
      status(res) shouldBe 200
    }
  }
}
