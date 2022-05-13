
package pages

import helpers.A11ySpec
import views.html.ErrorTemplate

class ErrorTemplateA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[ErrorTemplate]

  "the error template page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view("title", "heading", "message")(request, messages, config).toString must passAccessibilityChecks
      }
    }
  }
}