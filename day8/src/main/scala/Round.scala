import scala.collection.mutable.HashSet
import scala.io.Source
import scala.math._
object Rounds {
  private case class Tree(var value:Int, var view:Array[Int]) {
    def isVisible = view.exists(v => value > v)
  }
  private val shift: List[(Int, Int)] = List((-1, 0), (1, 0), (0, -1), (0, 1))

  private def getMaxViewSizeOrValue(direction : Int, matrix : Array[Array[Tree]], x:Int = 0, y:Int = 0) : Int = {
    val (maxY, maxX) = (matrix.apply(0).length - 1, matrix.length - 1)
    if ( (x > -1 && x <= maxX) && (y > -1 && y <= maxY) ) {
      val tree = matrix.apply(y).apply(x)
      if (tree.view.apply(direction) == -1) tree.view.update(direction,
          getMaxViewSizeOrValue(direction, matrix, x + shift(direction)._1, y + shift(direction)._2)
      )
      max(tree.view.apply(direction), tree.value)
    } else -1
  }

  private def fillMaxViewSizes(matrix : Array[Array[Tree]], x:Int = 0, y:Int = 0) : Tree = {
    Range(0, 4).foreach {
      getMaxViewSizeOrValue(_, matrix, x, y)
    }
    matrix.apply(y).apply(x)
  }

  def main(args: Array[String]): Unit = {
    var treeMatrix: Array[Array[Tree]] =
      Source.fromFile("input.txt").getLines.toArray
        .map(_.toArray.map(x => Tree(x.asDigit, Array.fill(4)(-1))))

    val visibleTrees: HashSet[Tree] = HashSet.empty
    for(y <- treeMatrix.indices; x <- treeMatrix.apply(y).indices) {
      val tree = fillMaxViewSizes(treeMatrix, x, y)
      if (tree.isVisible) visibleTrees.add(tree)
    }

    println(s"Round 1 : ${visibleTrees.size}")
  }
}
