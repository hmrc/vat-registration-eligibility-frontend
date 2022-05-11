package pages

import forms.TaxableSuppliesInUkFormProvider
import helpers.A11ySpec
import models.NormalMode
import views.html.TaxableSuppliesInUk

class TaxableSuppliesInUkA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[TaxableSuppliesInUk]
  val form = app.injector.instanceOf[TaxableSuppliesInUkFormProvider]

  "the Taxable Supplies In UK page" when {
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
