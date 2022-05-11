package pages

import forms.ThresholdTaxableSuppliesFormProvider
import helpers.A11ySpec
import models.NormalMode
import views.html.ThresholdTaxableSupplies

class ThresholdTaxableSuppliesA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[ThresholdTaxableSupplies]
  val form = app.injector.instanceOf[ThresholdTaxableSuppliesFormProvider]

  "the Threshold (taxable supplies) page" when {
    "the page is rendered without errors" when {
      "pass all accessibility tests" in {
        view(form(), NormalMode).toString must passAccessibilityChecks
      }
    }
    "the page is rendered with errors" must {
      "pass all accessibility test" in {
        view(form().bind(Map("value" -> "")), NormalMode).toString must passAccessibilityChecks
      }
    }
  }

}
