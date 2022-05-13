
package pages

import helpers.A11ySpec
import views.html.MtdInformation

class MtdInformationA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[MtdInformation]

  "the mtd information page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view()(request, messages, config).toString must passAccessibilityChecks
      }
    }
  }
}