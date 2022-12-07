import scala.io.Source

object Counts extends App {
  for (line <- Source.fromFile("input.txt").getLines) {
      println(line)
  }
}
