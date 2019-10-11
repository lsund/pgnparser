import scala.io.Source
import scala.util.parsing.combinator._

case class Metadata(key: String, value: String) {
  override def toString = s"($key,$value)"
}

case class Turn(number: Int, white: String, black: String) {
  override def toString = s"$number. $white $black"
}

case class Pgn(metadatas: List[Metadata], turns: List[Turn], score: String) {
  override def toString = s"$metadatas - $turns score: $score"
}

class SimpleParser extends RegexParsers {

  override def skipWhitespace = false

  // Metadata
  def openBrace: Parser[String] = """\[""".r ^^ { _.toString }
  def closingBrace: Parser[String] = """\]""".r ^^ { _.toString }
  def key: Parser[String] = """[a-zA-Z]+\s""".r ^^ { _.toString }
  def value: Parser[String] = """\".+\"""".r ^^ { _.toString }
  def metadata: Parser[Metadata] = openBrace ~ key ~ value ~ closingBrace ^^ {
    case _ ~ k ~ v ~ _ => Metadata(k, v)
  }

  // Turn
  def number: Parser[Int] = """[0-9]+""".r ^^ { _.toInt }
  def ply: Parser[String] = """\s[NBRQKa-h]?x?[a-h1-8]?[a-h][1-8]\+?\s""".r ^^ { _.toString }
  def clock: Parser[String] =
    """\{ \[%clk [0-9]:[0-9]{2}:[0-9]{2}\] \}""".r ^^ {
      _.toString
    }
  def turn: Parser[Turn] = number ~ "." ~ ply ~ clock ~ ply ~ clock ^^ {
    case n ~ p ~ w ~ c ~ b ~ c2 => Turn(n, w.trim, b.trim)
  }

  def score: Parser[String] = """\s*[0-1]-[0-1]""".r

  def eol = """(\r?\n)+""".r

  def any = ".*".r

  def pgn: Parser[Pgn] =
    repsep(metadata, eol) ~ "\n\n" ~ repsep(turn, " ") ~ score ^^ {
      case ms ~ ss ~ ts ~ sc => Pgn(ms, ts, sc)
    }

}

object RunParser extends SimpleParser {
  def main(args: Array[String]): Unit = {
    val lines = Source.fromFile("/home/lsund/file.txt").mkString
    parse(pgn, lines) match {
      case Success(matched, _) => println(matched)
      case Failure(msg, _)     => println(s"FAILURE: $msg")
      case Error(msg, _)       => println(s"ERROR: $msg")
    }
  }
}

object Main extends App {
  RunParser.main(Array())
}
