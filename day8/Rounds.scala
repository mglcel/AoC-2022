import scala.io.Source

object Rounds {
  def main(args: Array[String]): Unit = {
    for (line <- Source.fromFile("input.txt").getLines) {
      println(line)
    }
  }
}
