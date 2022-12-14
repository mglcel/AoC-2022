import java.io.File
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.text.format

typealias Node = Pair<Int, Int>
typealias Rope = Pair<Node, List<Node>>

private val originNode = Pair(0, 0)
private val originRope =
private val directionCodes = hashMapOf("L" to 0, "R" to 1, "U" to 2, "D" to 3)
private val shifts = listOf(Pair(-1, 0), Pair(1, 0), Pair(0, 1), Pair(0, -1))

fun main() {

    fun isNear(node1: Node, node2: Node): Boolean {
        return sqrt((node2.first - node1.first).toDouble().pow(2)
                    + (node2.second - node1.second).toDouble().pow(2)) < 2
    }

    fun shiftToDirection(node: Node, direction: Int): Node {
        return Pair(node.first + shifts[direction].first, node.second + shifts[direction].second)
    }

    fun shiftToRef(node: Node, ref: Node): Node {
        val (decX, decY) = (ref.first - node.first) to (ref.second - node.second)
        return Pair(
            node.first + if(abs(decX) == 2) decX / 2 else decX,
            node.second + if(abs(decY) == 2) decY / 2 else decY)
    }

    // ------------------------------------------------------------------------

    fun reconnectNodes(first: Node, second: Node) : Pair<Node, Node> {
        return if (!isNear(first, second)) Pair(first, shiftToRef(second, first))
        else Pair(first, second)
    }

    fun moveRope(currentRope: Rope, direction: Int) : Rope {
        val head = shiftToDirection(currentRope.first, direction)
        val newNodes = mutableListOf<Node>()
        var first = head
        currentRope.second.forEach {
            newNodes.add(reconnectNodes(first, it).second)
            first = newNodes.last()
        }
        return Pair(head, newNodes)
    }

    fun recordTail(rope: Rope, seenPositions: MutableSet<Node>) : Rope {
        seenPositions.add(rope.second.last())
        return rope
    }

    fun getSeenPositionsByTail(nbNodes: Int, motions: List<Pair<String, String>>): Int {
        val rope = Pair(originNode, List(nbNodes){originNode})
        val seenPositions = mutableSetOf<Node>()
        var nextRope = recordTail(rope, seenPositions)
        motions.forEach { motion ->
            repeat(motion.second.toInt()) {
                nextRope = recordTail(moveRope(nextRope, directionCodes[motion.first]!!), seenPositions)
            }
        }
        return seenPositions.size
    }

    // ------------------------------------------------------------------------

    var motions = File("input.txt").bufferedReader().readLines().map {
            it.split(" ").zipWithNext().single() }

    println(String.format("Round 1: %d", getSeenPositionsByTail(1, motions)))
    println(String.format("Round 2: %d", getSeenPositionsByTail(9, motions)))
}
