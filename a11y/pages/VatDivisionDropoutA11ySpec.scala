package pages

import helpers.A11ySpec
import views.html.VatDivisionDropout

class VatDivisionDropoutA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[VatDivisionDropout]

  "the VAT Divisiou Dropout page" must {
    "pass all accessibility tests" in {
      view()(request, messages, config).toString must passAccessibilityChecks
    }
  }

}
