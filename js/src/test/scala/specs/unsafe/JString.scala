package specs.unsafe

import org.scalacheck.Prop._
import specs.UTestScalaCheck
import utest._

object JString extends TestSuite with UTestScalaCheck {

  val tests = TestSuite {
    "The unsafe.JString value should" - {
      "read a String" - readStringJString
      "convert to jsAny" - toJsAny
      "convert toStandard" - toStandard
    }
  }

  def readStringJString =
    forAll { s: String =>
      scala.json.ast.unsafe.JString(s).value == s
    }.checkUTest()

  def toJsAny =
    forAll { s: String =>
      scala.json.ast.unsafe.JString(s).toJsAny == s
    }.checkUTest()

  def toStandard =
    forAll { s: String =>
      scala.json.ast.unsafe.JString(s).toStandard == scala.json.ast.JString(s)
    }.checkUTest()
}
