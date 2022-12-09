import scala.collection.mutable.SortedSet
import scala.collection.mutable.HashSet
import scala.io.Source.fromFile
import scala.math._

object Rounds {

  private type TreeMatrix = Array[Array[Tree]]
  private type Coordinates = (Int, Int)

  private case class Forest(var trees: TreeMatrix, max: (Int, Int)) {
    def hasTreeAt(pos: Coordinates) = (pos._1 > -1 && pos._1 <= max._1) && (pos._2 > -1 && pos._2 <= max._2)
    def tree(pos: Coordinates) = trees(pos._2)(pos._1)
  }

  private case class Tree(var height:Int, var space:Array[Int]) {
    def isVisible = space.exists(v => height > v)
  }

  private val Directions = Range(0, 4)
  private val shifts: List[(Int, Int)] = List((-1, 0), (1, 0), (0, -1), (0, 1))
  private def shift(pos: Coordinates)(implicit direction: Int) =
    (pos._1 + shifts(direction)._1, pos._2 + shifts(direction)._2)

  private def getMaxSpace(pos:Coordinates)(implicit forest : Forest, direction : Int) : Int = {
    if ( forest.hasTreeAt(pos) ) {
      val tree = forest.tree(pos)
      if (tree.space(direction) == -1)
        tree.space.update(direction, getMaxSpace(shift(pos)))
      max(tree.space(direction), tree.height)
    } else -1
  }

  private def fillTreeWithSpaces(pos:Coordinates)(implicit forest : Forest) : Tree = {
    Directions.foreach { implicit direction => getMaxSpace(pos) }
    forest.tree(pos)
  }

  private def treeDistance(pos:Coordinates, height:Int = 0)(implicit forest : Forest, direction : Int) : Int = {
    if ( forest.hasTreeAt(pos) )
      if (forest.tree(pos).height < height)
        1 + treeDistance(shift(pos), height)
      else 1
    else 0
  }

  private def scenicScore(pos:Coordinates)(implicit forest : Forest): Int =
    Directions.map(implicit direction => treeDistance(shift(pos), forest.tree(pos).height)).product

  def main(args: Array[String]): Unit = {
    var treeMatrix = fromFile("input.txt").getLines.toArray
      .map(_.toArray.map(x => Tree(x.asDigit, Array.fill(4)(-1))))

    implicit val forest = Forest(
      trees = treeMatrix,
      max = (treeMatrix(0).length - 1, treeMatrix.length - 1)
    )

    val visibleTrees: HashSet[Tree] = HashSet.empty
    for (y <- treeMatrix.indices; x <- treeMatrix(y).indices) {
      val tree = fillTreeWithSpaces((x, y))
      if (tree.isVisible) visibleTrees.add(tree)
    }

    val scenicScores: SortedSet[Int] = SortedSet.empty
    for (y <- treeMatrix.indices; x <- treeMatrix(y).indices)
      scenicScores.add(scenicScore((x, y)))

    println(s"Round 1 : ${visibleTrees.size}")
    println(s"Round 2 : ${scenicScores.last}")
  }
}
