package pages

import forms.VATRegistrationExceptionFormProvider
import helpers.A11ySpec
import models.NormalMode
import views.html.VatRegistrationException

class VatRegistrationExceptionA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[VatRegistrationException]
  val form = app.injector.instanceOf[VATRegistrationExceptionFormProvider]

  "the VAT Registration Exception page" when {
    "rendered without errors" must {
      "pass all accessibility tests" in {
        view(form(), NormalMode)(request, messages, config).toString must passAccessibilityChecks
      }
    }
    "rendered with errors" must {
      "pass all accessibility tests" in {
        view(form().bind(Map("value" -> "")), NormalMode)(request, messages, config).toString must passAccessibilityChecks
      }
    }
  }

}
