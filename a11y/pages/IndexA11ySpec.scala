
package pages

import helpers.A11ySpec
import views.html.Index

class IndexA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[Index]

  "the index page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view()(request, messages, config).toString must passAccessibilityChecks
      }
    }
  }
}