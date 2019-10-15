scalaVersion := "2.12.10"
name := "pgnparser"
organization := "com.github.lsund"
version := "1.0.0-SNAPSHOT"

// Dependencies
libraryDependencies += "org.typelevel" %% "cats-core" % "2.0.0"

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"

val circeVersion = "0.12.2"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies += "com.github.scopt" %% "scopt" % "4.0.0-RC2"

// Publishing information
homepage := Some(url("https://github.com/lsund/pgnparser"))
scmInfo := Some(ScmInfo(url("https://github.com/lsund/pgnparser"),
                            "git@github.com:lsund/pgnparser.git"))
developers := List(Developer("lsund",
                             "Ludvig Sundström",
                             "lud.sund@gmail.com",
                             url("https://github.com/lsund")))
licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
publishMavenStyle := true

publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)
