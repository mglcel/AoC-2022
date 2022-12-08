import scala.collection.mutable.SortedSet
import scala.collection.mutable.HashSet
import scala.io.Source.fromFile
import scala.math._

object Rounds {
  private case class Tree(var height:Int, var space:Array[Int]) {
    def isVisible = space.exists(v => height > v)
  }
  private val shift: List[(Int, Int)] = List((-1, 0), (1, 0), (0, -1), (0, 1))

  private def getMaxViewSizeOrValue(direction : Int, matrix : Array[Array[Tree]], x:Int = 0, y:Int = 0) : Int = {
    val (maxY, maxX) = (matrix(0).length - 1, matrix.length - 1)
    if ( (x > -1 && x <= maxX) && (y > -1 && y <= maxY) ) {
      val tree = matrix(y)(x)
      if (tree.space(direction) == -1) tree.space.update(direction,
          getMaxViewSizeOrValue(direction, matrix, x + shift(direction)._1, y + shift(direction)._2)
      )
      max(tree.space(direction), tree.height)
    } else -1
  }

  private def fillMaxViewSizes(matrix : Array[Array[Tree]], x:Int = 0, y:Int = 0) : Tree = {
    Range(0, 4).foreach { getMaxViewSizeOrValue(_, matrix, x, y) }
    matrix(y)(x)
  }

  private def getTreeDistance(direction : Int, matrix : Array[Array[Tree]], x:Int = 0, y:Int = 0, height:Int = 0) : Int = {
    val (maxY, maxX) = (matrix(0).length - 1, matrix.length - 1)
    if ( (x > -1 && x <= maxX) && (y > -1 && y <= maxY) ) {
      val tree = matrix(y)(x)
      if (tree.height < height) {
        1 + getTreeDistance(direction, matrix, x + shift(direction)._1, y + shift(direction)._2, height)
      } else 1
    } else 0
  }

  private def getScenicScore(matrix: Array[Array[Tree]], x: Int = 0, y: Int = 0): Int = {
    var scenicScore = 1
    Range(0, 4).foreach { distance =>
      scenicScore *= getTreeDistance(distance, matrix, x + shift(distance)._1, y + shift(distance)._2, matrix(y)(x).height)
    }
    scenicScore
  }

  def main(args: Array[String]): Unit = {
    var treeMatrix: Array[Array[Tree]] =
      fromFile("input.txt").getLines.toArray
        .map(_.toArray.map(x => Tree(x.asDigit, Array.fill(4)(-1))))

    val visibleTrees: HashSet[Tree] = HashSet.empty
    for(y <- treeMatrix.indices; x <- treeMatrix(y).indices) {
      val tree = fillMaxViewSizes(treeMatrix, x, y)
      if (tree.isVisible) visibleTrees.add(tree)
    }

    val scenicScores: SortedSet[Int] = SortedSet.empty
    for(y <- treeMatrix.indices; x <- treeMatrix(y).indices) {
      scenicScores.add(getScenicScore(treeMatrix, x, y))
    }

    println(s"Round 1 : ${visibleTrees.size}")
    println(s"Round 2 : ${scenicScores.last}")
  }
}
