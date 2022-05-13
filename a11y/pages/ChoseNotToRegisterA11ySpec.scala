
package pages

import helpers.A11ySpec
import views.html.ChoseNotToRegister

class ChoseNotToRegisterA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[ChoseNotToRegister]

  "the chose not to register page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view().toString must passAccessibilityChecks
      }
    }
  }
}
