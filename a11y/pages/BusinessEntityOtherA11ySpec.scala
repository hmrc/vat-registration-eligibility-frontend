
package pages

import models.BusinessEntity._

import helpers.A11ySpec
import views.html.BusinessEntityOther
import forms.BusinessEntityOtherFormProvider

class BusinessEntityOtherA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[BusinessEntityOther]
  val form = app.injector.instanceOf[BusinessEntityOtherFormProvider]

  "the other business entity page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view(form(), testCall)(request, messages, config).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors for missing business entity" must {
      "pass all accessibility tests" in {
        view(form().bind(Map("value" -> "")), testCall)(request, messages, config).toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors for invalid business entity" must {
      "pass all accessibility tests" in {
        view(form().bind(Map("value" -> `overseasKey`)), testCall)(request, messages, config).toString must passAccessibilityChecks
      }
    }
  }
}
