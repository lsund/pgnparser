scalaVersion := "2.13.1"
name := "pgnparser"
organization := "me.lsund"
version := "1.0"

libraryDependencies += "org.typelevel" %% "cats-core" % "2.0.0"

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"

val circeVersion = "0.12.2"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies += "com.github.scopt" %% "scopt" % "4.0.0-RC2"
