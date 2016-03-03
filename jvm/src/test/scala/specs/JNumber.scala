package specs

import scala.json.ast.{JNull, JNumber}

class JNumber extends Spec {
  def is =
    s2"""
  The JNumber value should
    read a Long $readLongJNumber
    read a BigDecimal $readBigDecimalJNumber
    read a BigInt $readBigIntJNumber
    read an Int $readIntJNumber
    read a Double $readDoubleJNumber
    read a Double NaN $readDoubleNANJNumber
    read a Double Positive Infinity $readDoublePositiveInfinityJNumber
    read a Double Negative Infinity $readDoubleNegativeInfinityJNumber
    read a Float $readFloatJNumber
    read a Short $readShortJNumber
  """

  private[this] final val mc = BigDecimal.defaultMathContext

  def readLongJNumber = prop { l: Long =>
    JNumber(l).value must beEqualTo(BigDecimal(l))
  }

  def readBigDecimalJNumber = prop { b: BigDecimal =>
    JNumber(b).value must beEqualTo(b)
  }

  def readBigIntJNumber = prop { b: BigInt =>
    JNumber(b).value must beEqualTo(BigDecimal(b))
  }

  def readIntJNumber = prop { i: Int =>
    JNumber(i).value must beEqualTo(BigDecimal(i))
  }

  def readDoubleJNumber = prop { d: Double =>
    JNumber(d) match {
      case JNull => JNull must beEqualTo(JNull)
      case JNumber(value) => value must beEqualTo(BigDecimal(d))
    }
  }

  def readDoubleNANJNumber = {
    JNumber(Double.NaN) match {
      case JNull => true
      case _ => false
    }
  }

  def readDoublePositiveInfinityJNumber = {
    JNumber(Double.PositiveInfinity) match {
      case JNull => true
      case _ => false
    }
  }

  def readDoubleNegativeInfinityJNumber = {
    JNumber(Double.NegativeInfinity) match {
      case JNull => true
      case _ => false
    }
  }

  def readFloatJNumber = prop { f: Float =>
    JNumber(f).value must beEqualTo(new BigDecimal(new java.math.BigDecimal(java.lang.Float.toString(f), mc), mc))
  }

  def readShortJNumber = prop { s: Short =>
    JNumber(s).value must beEqualTo(BigDecimal(s))
  }
}
