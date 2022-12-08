import scala.collection.mutable.SortedSet
import scala.collection.mutable.HashSet
import scala.io.Source.fromFile
import scala.math._

object Rounds {

  private type TreeMatrix = Array[Array[Tree]]
  private type Coordinates = (Int, Int)

  private case class Forest(var trees: TreeMatrix, maxX: Int, maxY: Int) {
    def hasTreeAt(pos: Coordinates) = (pos._1 > -1 && pos._1 <= maxX) && (pos._2 > -1 && pos._2 <= maxY)
    def tree(pos: Coordinates) = trees(pos._2)(pos._1)
  }

  private case class Tree(var height:Int, var space:Array[Int]) {
    def isVisible = space.exists(v => height > v)
  }

  private val shift: List[(Int, Int)] = List((-1, 0), (1, 0), (0, -1), (0, 1))
  private def doShift(direction: Int, pos: Coordinates) =
    (pos._1 + shift(direction)._1, pos._2 + shift(direction)._2)

  private def getMaxViewSize(direction : Int, forest : Forest, pos:Coordinates) : Int = {
    if ( forest.hasTreeAt(pos) ) {
      val tree = forest.tree(pos)
      if (forest.tree(pos).space(direction) == -1) tree.space.update(direction,
          getMaxViewSize(direction, forest, doShift(direction, pos))
      )
      max(tree.space(direction), tree.height)
    } else -1
  }

  private def fillMaxViewSizes(forest : Forest, pos:Coordinates) : Tree = {
    Range(0, 4).foreach { getMaxViewSize(_, forest, pos) }
    forest.tree(pos)
  }

  private def getTreeDistance(direction : Int, forest : Forest, pos:Coordinates, height:Int = 0) : Int = {
    if ( forest.hasTreeAt(pos) ) {
      if (forest.tree(pos).height < height) {
        1 + getTreeDistance(direction, forest, doShift(direction, pos), height)
      } else 1
    } else 0
  }

  private def getScenicScore(forest: Forest, pos:Coordinates): Int = {
    Range(0, 4).map(direction => getTreeDistance(direction, forest,
        doShift(direction, pos), forest.tree(pos).height)).product
  }

  def main(args: Array[String]): Unit = {
    var treeMatrix = fromFile("input.txt").getLines.toArray
        .map(_.toArray.map(x => Tree(x.asDigit, Array.fill(4)(-1))))

    val forest = Forest(
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
