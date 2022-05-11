package pages

import forms.VATExemptionFormProvider
import helpers.A11ySpec
import models.NormalMode
import views.html.VatExemption

class VatExemptionA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[VatExemption]
  val form = app.injector.instanceOf[VATExemptionFormProvider]

  "the VAT Exemption page" when {
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
