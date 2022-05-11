package pages

import forms.VATNumberFormProvider
import helpers.A11ySpec
import views.html.VATNumber

class VatNumberA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[VATNumber]
  val form = app.injector.instanceOf[VATNumberFormProvider]

  val testTogcCole = "togc"

  "the VAT Number page" when {
    "rendered without errors" must {
      "pass all accessibility tests" in {
        view(form = form(testTogcCole), testTogcCole)(request, messages, config).toString must passAccessibilityChecks
      }
    }
    "rendered with errors" must {
      "pass all accessibility tests" in {
        view(form(testTogcCole).bind(Map("value" -> "")), testTogcCole)(request, messages, config).toString must passAccessibilityChecks
      }
    }
  }

}
