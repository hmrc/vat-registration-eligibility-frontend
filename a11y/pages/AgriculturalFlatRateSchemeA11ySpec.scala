
package pages

import forms.AgriculturalFlatRateSchemeFormProvider
import helpers.A11ySpec
import models.NormalMode
import views.html.AgriculturalFlatRateScheme

class AgriculturalFlatRateSchemeA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[AgriculturalFlatRateScheme]
  val form = app.injector.instanceOf[AgriculturalFlatRateSchemeFormProvider]

  "the agricultural flat rate scheme page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view(form(), NormalMode).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors for missing flat rate scheme" must {
      "pass all accessibility test" in {
        view(form().bind(Map("value" -> "")), NormalMode).toString must passAccessibilityChecks
      }
    }
  }
}
