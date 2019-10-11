package com.github.lsund.pgnparser

import scala.io.Source
import scala.util.parsing.combinator._
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import java.nio.file.{Paths, Files}, java.nio.charset.StandardCharsets
import java.io.File
import scopt.OParser

case class Config(
    pgn: String = "test.pgn",
    out: String = "test.json"
) {
  override def toString = s"Config[$pgn, $out]"
}

case class Metadata(key: String, value: String) {
  override def toString = s"($key,$value)"
}

case class Turn(number: Int, white: String, black: String) {
  override def toString = s"$number. $white $black"
}

case class Pgn(metadata: List[Metadata], turns: List[Turn], score: String) {
  override def toString = s"$metadata - $turns score: $score"
}

class PgnParser extends RegexParsers {

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
  def ply: Parser[String] = """\s[NBRQKa-h]?x?[a-h1-8]?[a-h][1-8]\+?\s""".r ^^ {
    _.toString
  }
  def clock: Parser[String] =
    """\{ \[%clk [0-9]:[0-9]{2}:[0-9]{2}\] \}""".r ^^ {
      _.toString
    }
  def turn: Parser[Turn] = number ~ "." ~ ply ~ clock ~ ply ~ clock ^^ {
    case n ~ p ~ w ~ c ~ b ~ c2 => Turn(n, w.trim, b.trim)
  }

  // Score
  def score: Parser[String] = """\s*[0-1]-[0-1]""".r

  // Util
  def eol = """(\r?\n)+""".r
  def ws = """\s*""".r

  // Pgn
  def pgn: Parser[Pgn] =
    repsep(metadata, eol) ~ "\n\n" ~ repsep(turn, ws) ~ score ^^ {
      case ms ~ ss ~ ts ~ sc => Pgn(ms, ts, sc)
    }
}

object RunParser extends PgnParser {
  def main(args: Array[String]): Unit = {
    val builder = OParser.builder[Config]
    val optsparser = {
      import builder._
      OParser.sequence(
        programName("scopt"),
        head("scopt", "4.x"),
        builder
          .opt[String]('p', "pgn")
          .action((x, c) => c.copy(pgn = x))
          .text("PGN File to parse"),
        builder
          .opt[String]('o', "out")
          .action((x, c) => c.copy(out = x))
          .text("output JSON file")
      )
    }
    OParser.parse(optsparser, args, Config()) match {
      case Some(Config(pgnfile, outfile)) =>
        parse(pgn, Source.fromFile(pgnfile).mkString) match {
          case Success(matched, _) =>
            Files.write(
              Paths.get(outfile),
              matched.asJson.noSpaces.getBytes(StandardCharsets.UTF_8)
            )
            println("Wrote file: " + outfile)
          case Failure(msg, _) => println(s"FAILURE: $msg")
          case Error(msg, _)   => println(s"ERROR: $msg")
        }
      case _ =>
    }
  }
}
