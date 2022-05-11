package pages

import forms.VATExceptionKickoutFormProvider
import helpers.A11ySpec
import models.NormalMode
import views.html.VatExceptionKickout

class VatExceptionKickoutA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[VatExceptionKickout]
  val form = app.injector.instanceOf[VATExceptionKickoutFormProvider]

  "the VAT Exception Kickout page" when {
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
