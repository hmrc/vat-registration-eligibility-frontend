
package pages

import forms.DateOfBusinessTransferFormProvider
import helpers.A11ySpec
import models.NormalMode
import views.html.DateOfBusinessTransfer

class DateOfBusinessTransferA11ySpec extends A11ySpec {

  val view = app.injector.instanceOf[DateOfBusinessTransfer]
  val form = app.injector.instanceOf[DateOfBusinessTransferFormProvider]

  "the date of business transfer page" when {
    "the page is rendered without errors" must {
      "pass all accessibility tests" in {
        view(form("key").bind(
          Map(
            "relevantDate.day" -> s"${testDate.getDayOfMonth}",
            "relevantDate.month" -> s"${testDate.getMonthValue}",
            "relevantDate.year" -> s"${testDate.getYear}"
          )
        ), NormalMode, "key").toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors on missing date value" must {
      "pass all accessibility tests" in {
        view(form("key").bind(Map("" -> "")), NormalMode, "key").toString must passAccessibilityChecks
      }
    }

    "the page is rendered with errors on invalid date value" must {
      "pass all accessibility tests" in {
        view(form("key").bind(
          Map(
            "relevantDate.day" -> s"${testDate.getDayOfMonth}",
            "relevantDate.month" -> s"test",
            "relevantDate.year" -> s"${testDate.getYear}"
          )
        ), NormalMode, "key").toString must passAccessibilityChecks
      }
    }
  }
}
