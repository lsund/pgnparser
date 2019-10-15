package com.github.lsund.pgnparser

import scala.io.Source
import scala.util.parsing.combinator._
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import java.nio.file.{Paths, Files}, java.nio.charset.StandardCharsets
import java.io.File
import scopt.OParser

case class Metadata(key: String, value: String) {}

case class Turn(number: Int, white: String, black: String) {}

case class Game(metadata: List[Metadata], turns: List[Turn], score: String) {}

class PgnParser extends RegexParsers {

  override def skipWhitespace = false

  // Util
  def eol = """(\r?\n)+""".r
  def ws = """\s*""".r

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

  // Game
  def game: Parser[Game] =
    repsep(metadata, eol) ~ "\n\n" ~ repsep(turn, ws) ~ score ^^ {
      case ms ~ ss ~ ts ~ sc => Game(ms, ts, sc)
    }
}

case class CliOptions(
    pgn: String = "",
    out: String = ""
) {
  override def toString = s"CliOptions[$pgn, $out]"
}

object ParseRunner extends PgnParser {

  def generateJson(pgnfile: String): String = {
    parse(game, Source.fromFile(pgnfile).mkString) match {
      case Success(matched, _) =>
        matched.asJson.noSpaces
      case Failure(msg, _) => s"FAILURE: $msg"
      case Error(msg, _)   => s"ERROR: $msg"
    }
  }

  def writeJsonFile(pgnfile: String, outfile: String) {
    Files.write(
      Paths.get(outfile),
      generateJson(pgnfile).getBytes(StandardCharsets.UTF_8)
    )
  }

  def main(args: Array[String]): Unit = {
    val builder = OParser.builder[CliOptions]
    val optsparser = {
      import builder._
      OParser.sequence(
        programName("pgnparser"),
        head("pgnparser", "1.0.0"),
        builder
          .opt[String]('p', "pgn")
          .required()
          .action((x, c) => c.copy(pgn = x))
          .text("PGN File to parse"),
        builder
          .opt[String]('o', "out")
          .required()
          .action((x, c) => c.copy(out = x))
          .text("output JSON file")
      )
    }
    OParser.parse(optsparser, args, CliOptions()) match {
      case Some(CliOptions(pgnfile, outfile)) => writeJsonFile(pgnfile, outfile)
      case _                                  => ;
    }
  }
}
