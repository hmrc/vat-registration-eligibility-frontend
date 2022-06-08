
package pages

import forms.KeepOldVrnFormProvider
import helpers.A11ySpec
import views.html.KeepOldVrn

class KeepOldVrnA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[KeepOldVrn]
  val form = app.injector.instanceOf[KeepOldVrnFormProvider]

  "the keep old vrn page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view(form("key"), "key").toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors for missing form value" must {
      "pass all accessibility tests" in {
        view(form("key").bind(Map("value" -> "")), "key").toString must passAccessibilityChecks
      }
    }
  }
}