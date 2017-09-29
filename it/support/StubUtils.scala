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

package support

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.matching.UrlPathPattern
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.enums.VatRegStatus
import models.S4LKey
import play.api.libs.json.{Format, JsObject, JsString, Json}
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.crypto.CompositeSymmetricCrypto.aes
import uk.gov.hmrc.crypto.json.{JsonDecryptor, JsonEncryptor}

trait StubUtils {
  me: StartAndStopWireMock =>

  final class RequestHolder(var request: FakeRequest[AnyContentAsFormUrlEncoded])

  implicit lazy val jsonCrypto = ApplicationCrypto.JsonCrypto
  implicit lazy val encryptionFormat = new JsonEncryptor[JsObject]()
  implicit lazy val decryptionFormat = new JsonDecryptor[JsObject]()

  class PreconditionBuilder {

    implicit val builder: PreconditionBuilder = this

    def address(id: String, line1: String, line2: String, country: String, postcode: String) =
      AddressStub(id, line1, line2, country, postcode)

    def postRequest(data: Map[String, String])(implicit requestHolder: RequestHolder): PreconditionBuilder = {
      val requestWithBody = FakeRequest("POST", "/").withFormUrlEncodedBody(data.toArray: _*)
      requestHolder.request = requestWithBody
      this
    }

    def user = UserStub()

    def journey(id: String) = JourneyStub(id)

    def vatRegistrationFootprint = VatRegistrationFootprintStub()

    def vatScheme = VatSchemeStub()

    def corporationTaxRegistration = CorporationTaxRegistrationStub()

    def currentProfile = CurrentProfile()

    def company = IncorporationStub()

    def s4lContainer[C: S4LKey]: ViewModelStub[C] = new ViewModelStub[C]()

    def audit = AuditStub()

    def keystoreS = ksStub()

    def businessReg = BusinessRegStub()

  }

  def given(): PreconditionBuilder = {
    new PreconditionBuilder()
  }

case class ksStub()(implicit builder:PreconditionBuilder) extends KeystoreStub {
    def hasKeyStoreValue(key: String, data: String):PreconditionBuilder ={
      stubKeystoreGet(key,data)
      builder
    }

  def hasKeystoreValueWithKeyInUrl(key:String,data:String,urlKey:String):PreconditionBuilder = {
    stubKeystoreGetWithUrl(key,data,urlKey)
    builder
  }

  def putKeyStoreValueWithKeyInUrl(key:String,data:String,urlKey:String):PreconditionBuilder = {
    stubKeystorePutWithUrl(key,data,urlKey)
    builder
  }

  def putKeyStoreValue(key:String,data:String):PreconditionBuilder ={
    stubKeystorePut(key,data)
    builder
  }
  }

  case class BusinessRegStub()(implicit builder:PreconditionBuilder)  {
    def getBusinessprofileSuccessfully = {
      stubFor(
        get(urlPathEqualTo("/business-registration/business-tax-registration"))
          .willReturn((ok(
            """
              |{
              | "registrationID" : "1",
              | "language" : "EN"
              |}
              |"""".stripMargin)
            )
          )
      )
      builder
    }
  }

  trait KeystoreStub {
    def stubKeystorePut(key: String, data: String): StubMapping =
      stubFor(
        put(urlPathMatching(s"/keystore/vat-registration-eligibility-frontend/session-[a-z0-9-]+/data/$key"))
          .willReturn(ok(
            s"""
               |{ "atomicId": { "$$oid": "598ac0b64e0000d800170620" },
               |    "data": { "$key": $data },
               |    "id": "session-ac4ed3e7-dbc3-4150-9574-40771c4285c1",
               |    "modifiedDetails": {
               |      "createdAt": { "$$date": 1502265526026 },
               |      "lastUpdated": { "$$date": 1502265526026 }}}
            """.stripMargin
          )))

    def stubKeystorePutWithUrl(key: String, data: String,urlKey:String): StubMapping =
      stubFor(
        put(urlPathMatching(s"/keystore/vat-registration-eligibility-frontend/session-[a-z0-9-]+/data/$urlKey"))
          .willReturn(ok(
            s"""
               |{ "atomicId": { "$$oid": "598ac0b64e0000d800170620" },
               |    "data": { "$key": $data },
               |    "id": "session-ac4ed3e7-dbc3-4150-9574-40771c4285c1",
               |    "modifiedDetails": {
               |      "createdAt": { "$$date": 1502265526026 },
               |      "lastUpdated": { "$$date": 1502265526026 }}}
            """.stripMargin
          )))

    def stubKeystoreGet(key: String, data: String): StubMapping = {
      stubFor(
        get(urlPathMatching("/keystore/vat-registration-eligibility-frontend/session-[a-z0-9-]+"))
          .willReturn(ok(
            s"""
               |{ "atomicId": { "$$oid": "598ac0b64e0000d800170620" },
               |    "data": { "$key": $data },
               |    "id": "session-ac4ed3e7-dbc3-4150-9574-40771c4285c1",
               |    "modifiedDetails": {
               |      "createdAt": { "$$date": 1502265526026 },
               |      "lastUpdated": { "$$date": 1502265526026 }}}
            """.stripMargin
          )))

  }
  def stubKeystoreGetWithUrl(key:String,data:String,urlKey:String):StubMapping = {
    stubFor(
      get(urlPathMatching(s"/keystore/vat-registration-eligibility-frontend/session-[a-z0-9-]+/data/$urlKey"))
        .willReturn(ok(
          s"""
             |{ "atomicId": { "$$oid": "598ac0b64e0000d800170620" },
             |    "data": { "$key": $data },
             |    "id": "session-ac4ed3e7-dbc3-4150-9574-40771c4285c1",
             |    "modifiedDetails": {
             |      "createdAt": { "$$date": 1502265526026 },
             |      "lastUpdated": { "$$date": 1502265526026 }}}
            """.stripMargin
        )))

   }
  }

  trait S4LStub {

    import uk.gov.hmrc.crypto._

    //TODO get the json.encryption.key config value from application.conf
    val crypto: CompositeSymmetricCrypto = aes("fqpLDZ4sumDsekHkeEBlCA==", Seq.empty)

    def decrypt(encData: String): String = crypto.decrypt(Crypted(encData)).value

    def encrypt(str: String): String = crypto.encrypt(PlainText(str)).value


    def stubS4LPut(key: String, data: String): StubMapping =
      stubFor(
        put(urlPathMatching(s"/save4later/vat-registration-eligibility-frontend/1/data/$key"))
          .willReturn(ok(
            s"""
               |{ "atomicId": { "$$oid": "598ac0b64e0000d800170620" },
               |    "data": { "$key": "${encrypt(data)}" },
               |    "id": "1",
               |    "modifiedDetails": {
               |      "createdAt": { "$$date": 1502265526026 },
               |      "lastUpdated": { "$$date": 1502265526026 }}}
            """.stripMargin
          )))

    def stubS4LGet[C, T](t: T)(implicit key: S4LKey[C], fmt: Format[T]): StubMapping = {
      val s4lData = Json.toJson(t).as[JsObject]

      val s4lResponse = Json.obj(
        "id"   -> key.key,
        "data" -> Json.obj(
          key.key -> encryptionFormat.writes(Protected(s4lData))
        )
      )

      stubFor(
        get(urlPathMatching("/save4later/vat-registration-eligibility-frontend/1"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(s4lResponse.toString())
          ))
    }


    def stubS4LGetNothing(): StubMapping =
      stubFor(
        get(urlPathMatching("/save4later/vat-registration-eligibility-frontend/1"))
          .willReturn(ok(
            s"""
               |{
               |  "atomicId": { "$$oid": "598830cf5e00005e00b3401e" },
               |  "data": {},
               |  "id": "1",
               |  "modifiedDetails": {
               |    "createdAt": { "$$date": 1502097615710 },
               |    "lastUpdated": { "$$date": 1502189409725 }
               |  }
               |}
            """.stripMargin
          )))

  }


  class ViewModelStub[C]()(implicit builder: PreconditionBuilder, s4LKey: S4LKey[C]) extends S4LStub with KeystoreStub {

    def contains[T](t: T)(implicit fmt: Format[T]): PreconditionBuilder = {
      stubS4LGet[C, T](t)
      builder
    }


    def isUpdatedWith[T](t: T)(implicit key: S4LKey[C], fmt: Format[T]): PreconditionBuilder = {
      stubS4LPut(key.key, fmt.writes(t).toString())
      builder
    }

    def isEmpty: PreconditionBuilder = {
      stubS4LGetNothing()
      builder
    }

  }


  case class IncorporationStub
  ()
  (implicit builder: PreconditionBuilder) extends KeystoreStub {

    def isIncorporated: PreconditionBuilder = {

      stubFor(
        get(urlPathEqualTo("/vatreg/incorporation-information/000-434-1"))
          .willReturn(ok(
            s"""
               |{
               |  "statusEvent": {
               |    "crn": "90000001",
               |    "incorporationDate": "2016-08-05",
               |    "status": "accepted"
               |  },
               |  "subscription": {
               |    "callbackUrl": "http://localhost:9896/callbackUrl",
               |    "regime": "vat",
               |    "subscriber": "scrs",
               |    "transactionId": "000-434-1"
               |  }
               |}
             """.stripMargin
          ))
      )
      builder
    }

    def incorporationStatusNotKnown(): PreconditionBuilder = {
      stubFor(
        get(urlPathEqualTo(s"/vatreg/incorporation-information/000-434-1"))
          .willReturn(notFound().withBody(
            s"""
               |{
               |  "errorCode": 404,
               |  "errorMessage": "Incorporation Status not known. A subscription has been setup"
               |}
             """.stripMargin
          )))
      builder
    }
  }


  case class CorporationTaxRegistrationStub
  ()
  (implicit builder: PreconditionBuilder) {

    def existsWithStatus(status: String): PreconditionBuilder = {
      stubFor(
        get(urlPathEqualTo(s"/incorporation-frontend-stubs/1/corporation-tax-registration"))
          .willReturn(ok(
            s"""{ "confirmationReferences": { "transaction-id": "000-434-1" }, "status": "$status" }"""
          )))
      builder
    }

  }

  case class CurrentProfile()(implicit builder: PreconditionBuilder) extends KeystoreStub{
    def setup: PreconditionBuilder = {
      stubFor(
        get(urlPathEqualTo(s"/incorporation-information/000-434-1/company-profile"))
          .willReturn(ok(
            s"""{ "company_name": "testCompanyName" }"""
          )))

      stubFor(
        get(urlPathEqualTo("/vatreg/incorporation-information/000-434-1"))
          .willReturn(ok(
            s"""
               |{
               |  "statusEvent": {
               |    "crn": "90000001",
               |    "incorporationDate": "2016-08-05",
               |    "status": "accepted"
               |  },
               |  "subscription": {
               |    "callbackUrl": "http://localhost:9896/callbackUrl",
               |    "regime": "vat",
               |    "subscriber": "scrs",
               |    "transactionId": "000-434-1"
               |  }
               |}
             """.stripMargin
          )))

      stubFor(
        get(urlPathEqualTo("/business-registration/business-tax-registration"))
          .willReturn((ok(
                    """
                      |{
                      | "registrationID" : "1",
                      | "language" : "EN"
                      |}
                      |"""".stripMargin)
              )
            )
      )
      CorporationTaxRegistrationStub().existsWithStatus("held")


      stubKeystorePut("CurrentProfile",
        """
          |{
          | "companyName" : "testCompanyName",
          | "registrationID" : "1",
          | "transactionID" : "000-434-1",
          | "vatRegistrationStatus" : "DRAFT"
          |}
        """.stripMargin)

      builder
    }

    def withProfileAndIncorpDate = withProfileInclIncorp(true)
    def withProfile = withProfileInclIncorp(false)

   private val withProfileInclIncorp = (withIncorporationDate:Boolean) => {
      val incorporationDate = ""","incorporationDate": "2016-08-05"}"""
    val js = s"""
        |{
        | "companyName" : "testCompanyName",
        | "registrationID" : "1",
        | "transactionID" : "000-434-1",
        | "vatRegistrationStatus" : "${VatRegStatus.DRAFT}"
        """.stripMargin
     stubKeystoreGet("CurrentProfile",  if(withIncorporationDate) js + incorporationDate else js + "}")
      builder
    }
  }

  case class VatSchemeStub
  ()
  (implicit builder: PreconditionBuilder) extends KeystoreStub {

    def hasValidEligibilityData:PreconditionBuilder = {
      stubFor(
        get(urlPathEqualTo("/vatreg/1/get-scheme"))
          .willReturn(ok(
            s"""{ "registrationId" : "1",
               | "vatEligibility" : {
               |        "haveNino" : true,
               |        "doingBusinessAbroad" : false,
               |        "doAnyApplyToYou" : false,
               |        "applyingForAnyOf" : false,
               |        "companyWillDoAnyOf" : false
               |    }}""".stripMargin
          )))
      builder
    }



    def hasServiceEligibilityDataApartFromLastQuestion:PreconditionBuilder = {
      stubFor(
        get(urlPathEqualTo("/vatreg/1/service-eligibility"))
          .willReturn(ok(
            s"""{ "registrationId" : "1",
               | "vatEligibility" : {
               |        "haveNino" : true,
               |        "doingBusinessAbroad" : false,
               |        "doAnyApplyToYou" : false,
               |        "applyingForAnyOf" : false
               |    }}""".stripMargin
          )))
      builder
    }


    def isBlank: PreconditionBuilder = {
      stubFor(
        get(urlPathEqualTo("/vatreg/1/get-scheme"))
          .willReturn(ok(
            s"""{ "registrationId" : "1" }"""
          )))
      builder
    }

    def isUpdatedWith[T](t: T)(implicit tFmt: Format[T]) = {
      stubFor(
        patch(urlPathMatching(s"/vatreg/1/.*"))
          .willReturn(aResponse().withStatus(202).withBody(tFmt.writes(t).toString())))
      builder
    }

  }

  case class VatRegistrationFootprintStub
  ()
  (implicit builder: PreconditionBuilder) extends KeystoreStub {

    def exists: PreconditionBuilder = {
      stubFor(
        post(urlPathEqualTo("/vatreg/new"))
          .willReturn(ok(
            s"""{ "registrationId" : "1" }"""
          )))

      builder
    }

    def fails: PreconditionBuilder = {
      stubFor(
        post(urlPathEqualTo("/vatreg/new"))
          .willReturn(serverError()))

      builder
    }
  }

  case class UserStub
  ()
  (implicit builder: PreconditionBuilder) extends SessionBuilder {

    def isAuthorised(implicit requestHolder: RequestHolder): PreconditionBuilder = {
      requestHolder.request = requestWithSession(requestHolder.request, "anyUserId")
      stubFor(
        get(urlPathEqualTo("/auth/authority"))
          .willReturn(ok(
            s"""
               |{
               |  "uri":"anyUserId",
               |  "loggedInAt": "2014-06-09T14:57:09.522Z",
               |  "previouslyLoggedInAt": "2014-06-09T14:48:24.841Z",
               |  "credentials": {"gatewayId":"xxx2"},
               |  "accounts": {},
               |  "levelOfAssurance": "2",
               |  "confidenceLevel" : 50,
               |  "credentialStrength": "strong",
               |  "legacyOid": "1234567890",
               |  "userDetailsLink": "http://localhost:11111/auth/userDetails",
               |  "ids": "/auth/ids"
               |}""".stripMargin
          )))
      builder
    }

    def isNotAuthorised  = {
      stubFor(
        get(urlPathEqualTo("/auth/authority"))
          .willReturn(forbidden()))
      builder
     }
    }



  case class JourneyStub
  (journeyId: String)
  (implicit builder: PreconditionBuilder) {

    val journeyInitUrl: UrlPathPattern = urlPathMatching(s".*/api/init/$journeyId")

    def initialisedSuccessfully(): PreconditionBuilder = {
      stubFor(post(journeyInitUrl).willReturn(aResponse.withStatus(202).withHeader("Location", "continueUrl")))
      builder
    }

    def notInitialisedAsExpected(): PreconditionBuilder = {
      stubFor(post(journeyInitUrl).willReturn(aResponse().withStatus(202))) // a 202 _without_ Location header
      builder
    }

    def failedToInitialise(): PreconditionBuilder = {
      stubFor(post(journeyInitUrl).willReturn(serverError()))
      builder
    }

  }

  case class AddressStub
  (id: String, line1: String, line2: String, country: String, postcode: String)
  (implicit builder: PreconditionBuilder) {

    val confirmedAddressPath = s""".*/api/confirmed[?]id=$id"""

    def isFound: PreconditionBuilder = {
      stubFor(
        get(urlMatching(confirmedAddressPath))
          .willReturn(ok(
            s"""
               |{
               |  "auditRef": "$id",
               |  "id": "GB990091234520",
               |  "address": {
               |    "country": {
               |      "code": "GB",
               |      "name": "$country"
               |    },
               |    "lines": [
               |      "$line1",
               |      "$line2"
               |    ],
               |    "postcode": "$postcode"
               |  }
               |}
         """.stripMargin
          )))
      builder
    }

    def isNotFound: PreconditionBuilder = {
      stubFor(
        get(urlMatching(confirmedAddressPath))
          .willReturn(notFound()))
      builder
    }
  }
  case class AuditStub()
  (implicit builder: PreconditionBuilder) {
    def writesAudit(status:Int =200) = {
      stubFor(post(urlMatching("/write/audit"))
        .willReturn(
          aResponse().
            withStatus(status).
            withBody("""{"x":2}""")
        )
      )
      builder
    }
    def failsToWriteAudit() = {
      writesAudit(404)
    }
  }
}