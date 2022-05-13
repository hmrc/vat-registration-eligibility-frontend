
package pages

import helpers.A11ySpec
import views.html.ApplyInWriting

class ApplyInWritingA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[ApplyInWriting]

  "the apply in writing page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view()(request, messages, config).toString must passAccessibilityChecks
      }
    }
  }
}
