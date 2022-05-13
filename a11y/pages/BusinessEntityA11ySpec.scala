
package pages

import models.BusinessEntity._
import forms.BusinessEntityFormProvider
import helpers.A11ySpec
import views.html.BusinessEntityView

class BusinessEntityA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[BusinessEntityView]
  val form = app.injector.instanceOf[BusinessEntityFormProvider]

  "the business entity page" when {
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
