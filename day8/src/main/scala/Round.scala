import scala.collection.mutable.SortedSet
import scala.collection.mutable.HashSet
import scala.io.Source.fromFile
import scala.math._

object Rounds {

  private type TreeMatrix = Array[Array[Tree]]
  private type Coordinates = (Int, Int)
  private case class Forest(var trees: TreeMatrix, maxX: Int, maxY: Int) {
    def isWithin(pos: Coordinates) = (pos._1 > -1 && pos._1 <= maxX) && (pos._2 > -1 && pos._2 <= maxY)
  }

  private case class Tree(var height:Int, var space:Array[Int]) {
    def isVisible = space.exists(v => height > v)
  }

  private val shift: List[(Int, Int)] = List((-1, 0), (1, 0), (0, -1), (0, 1))

  private def getMaxViewSizeOrValue(direction : Int, forest : Forest, pos:Coordinates) : Int = {
    if ( forest.isWithin(pos) ) {
      val tree = forest.trees(pos._2)(pos._1)
      if (tree.space(direction) == -1) tree.space.update(direction,
          getMaxViewSizeOrValue(direction, forest,
            (pos._1 + shift(direction)._1, pos._2 + shift(direction)._2))
      )
      max(tree.space(direction), tree.height)
    } else -1
  }

  private def fillMaxViewSizes(forest : Forest, pos:Coordinates) : Tree = {
    Range(0, 4).foreach { getMaxViewSizeOrValue(_, forest, pos) }
    forest.trees(pos._2)(pos._1)
  }

  private def getTreeDistance(direction : Int, forest : Forest, pos:Coordinates, height:Int = 0) : Int = {
    if ( forest.isWithin(pos) ) {
      val tree = forest.trees(pos._2)(pos._1)
      if (tree.height < height) {
        1 + getTreeDistance(direction, forest,
          (pos._1 + shift(direction)._1, pos._2 + shift(direction)._2), height)
      } else 1
    } else 0
  }

  private def getScenicScore(forest: Forest, pos:Coordinates): Int = {
    var scenicScore = 1
    Range(0, 4).foreach { distance =>
      scenicScore *= getTreeDistance(distance, forest,
        (pos._1 + shift(distance)._1, pos._2 + shift(distance)._2),
        forest.trees(pos._2)(pos._1).height)
    }
    scenicScore
  }

  def main(args: Array[String]): Unit = {
    var treeMatrix = fromFile("input.txt").getLines.toArray
        .map(_.toArray.map(x => Tree(x.asDigit, Array.fill(4)(-1))))

    var forest = Forest(
      trees = treeMatrix,
      maxY = treeMatrix(0).length - 1,
      maxX = treeMatrix.length - 1)

    val visibleTrees: HashSet[Tree] = HashSet.empty
    for (y <- treeMatrix.indices; x <- treeMatrix(y).indices) {
      val tree = fillMaxViewSizes(forest, (x, y))
      if (tree.isVisible) visibleTrees.add(tree)
    }

    val scenicScores: SortedSet[Int] = SortedSet.empty
    for (y <- treeMatrix.indices; x <- treeMatrix(y).indices) {
      scenicScores.add(getScenicScore(forest, (x, y)))
    }

    println(s"Round 1 : ${visibleTrees.size}")
    println(s"Round 2 : ${scenicScores.last}")
  }
}
