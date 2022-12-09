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

  private val shifts: List[(Int, Int)] = List((-1, 0), (1, 0), (0, -1), (0, 1))
  private def shift(pos: Coordinates)(implicit direction: Int) =
    (pos._1 + shifts(direction)._1, pos._2 + shifts(direction)._2)

  private def getMaxViewSize(pos:Coordinates)(implicit forest : Forest, direction : Int) : Int = {
    if ( forest.hasTreeAt(pos) ) {
      val tree = forest.tree(pos)
      if (tree.space(direction) == -1)
        tree.space.update(direction, getMaxViewSize(shift(pos)))
      max(tree.space(direction), tree.height)
    } else -1
  }

  private def fillMaxViewSizes(pos:Coordinates)(implicit forest : Forest) : Tree = {
    Range(0, 4).foreach { implicit direction => getMaxViewSize(pos) }
    forest.tree(pos)
  }

  private def getTreeDistance(pos:Coordinates, height:Int = 0)(implicit forest : Forest, direction : Int) : Int = {
    if ( forest.hasTreeAt(pos) )
      if (forest.tree(pos).height < height)
        1 + getTreeDistance(shift(pos), height)
      else 1
    else 0
  }

  private def getScenicScore(pos:Coordinates)(implicit forest : Forest): Int = {
    Range(0, 4).map(implicit direction =>
      getTreeDistance(shift(pos), forest.tree(pos).height)).product
  }

  def main(args: Array[String]): Unit = {
    var treeMatrix = fromFile("input.txt").getLines.toArray
      .map(_.toArray.map(x => Tree(x.asDigit, Array.fill(4)(-1))))

    implicit val forest = Forest(
      trees = treeMatrix,
      max = (treeMatrix(0).length - 1, treeMatrix.length - 1)
    )

    val visibleTrees: HashSet[Tree] = HashSet.empty
    for (y <- treeMatrix.indices; x <- treeMatrix(y).indices) {
      val tree = fillMaxViewSizes((x, y))
      if (tree.isVisible) visibleTrees.add(tree)
    }

    val scenicScores: SortedSet[Int] = SortedSet.empty
    for (y <- treeMatrix.indices; x <- treeMatrix(y).indices)
      scenicScores.add(getScenicScore((x, y)))

    println(s"Round 1 : ${visibleTrees.size}")
    println(s"Round 2 : ${scenicScores.last}")
  }
}
