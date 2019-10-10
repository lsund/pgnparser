import scala.io.Source

object Main extends App {
  val lines = Source.fromFile("/home/lsund/file.txt").mkString
  lines.split("\n").foreach(println)
}
