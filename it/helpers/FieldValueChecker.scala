package helpers

import org.jsoup.nodes.Document

import java.time.LocalDate

trait FieldValueChecker {

  val checkedAttr = "checked"
  val valueAttr = "value"

  private object Selectors {
    private def baseInputSelector(inputType: String, id: String) = s"input[type=$inputType][id=$id]"
    def radio(id: String): String = baseInputSelector("radio", id)
    def textBox(id:String): String =  baseInputSelector("text", id)
    def dateFieldDay(id: String) = baseInputSelector("text", s"$id.day")
    def dateFieldMonth(id: String) = baseInputSelector("text", s"$id.month")
    def dateFieldYear(id: String) = baseInputSelector("text", s"$id.year")
  }

  implicit class EnhancedDoc(doc: Document) {
    def radioIsSelected(id: String): Boolean =
      doc.select(Selectors.radio(id)).hasAttr(checkedAttr)

    def textboxContainsValue(id: String, expectedValue: String): Boolean =
      doc.select(Selectors.textBox(id)).`val`() == expectedValue

    def dateFieldContainsValue(id: String, expectedValue: LocalDate, includeDay: Boolean = true): Boolean =
      { if (includeDay) doc.select(Selectors.dateFieldDay(id)).attr(valueAttr) == expectedValue.getDayOfMonth.toString else true } && {
        doc.select(Selectors.dateFieldMonth(id)).attr(valueAttr) == expectedValue.getMonthValue.toString &&
        doc.select(Selectors.dateFieldYear(id)).attr(valueAttr) == expectedValue.getYear.toString
      }
  }

}
