package day08

import println
import readInput

fun main() {
    fun part1(input: List<String>): Int {
        return Chart.parse(input).stepsCount()
    }

    fun part2(input: List<String>): Long {
        return Chart.parse(input).ghostPathStepsCount()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("day08/Day08_test")
    check(part1(testInput) == 2)
    val testInput2 = readInput("day08/Day08_test2")
    check(part1(testInput2) == 6)
    val testInput3 = readInput("day08/Day08_test3")
    check(part2(testInput3) == 6L)

    val input = readInput("day08/Day08")
    part1(input).println()
    part2(input).println()
}

data class Chart(
    val navigation: List<Direction>, // would be safer to use an array list to guarantee order
    val network: Map<NodeName, Node>
) {
    private val networkStart = NodeName("AAA") // part1
    private val networkEnd = NodeName("ZZZ") // part1

    private val networkStartSet: Set<NodeName> = network.keys.filter(NodeName::isStartNode).toSet() // part2
    private val networkEndSet: Set<NodeName> = network.keys.filter(NodeName::isEndNode).toSet() // part2

    fun stepsCount(): Int {
        var stepCount = 0
        var currNodeName = networkStart
        var i = 0
        while (currNodeName != networkEnd) {
            stepCount++
            val node = network[currNodeName] ?: throw IllegalStateException("Could not find node $currNodeName")
            currNodeName = when (navigation[i]) {
                Direction.LEFT -> node.left
                Direction.RIGHT -> node.right
            }
            i = (i + 1) % navigation.size // mod on size to keep cycling
        }
        return stepCount
    }

    fun ghostPathStepsCount(): Long {
        var i = 0
        val eachStartToAnEndSteps = mutableListOf<Pair<NodeName, Long>>()

        networkStartSet.forEach { nodeName ->
            var stepCount = 0L
            var currNodeName = nodeName
            while (!networkEndSet.contains(currNodeName)) {
                stepCount++
                val currNavigation = navigation[i]
                val node = network[currNodeName] ?: throw IllegalStateException("Could not find node $currNodeName")
                currNodeName = when (currNavigation) {
                    Direction.LEFT -> node.left
                    Direction.RIGHT -> node.right
                }
                i = (i + 1) % navigation.size // mod on size to keep cycling
            }
            eachStartToAnEndSteps.add(Pair(nodeName, stepCount))
        }

        // The minimum number of steps is the Largest Common Multiplier (LCM)
        // of all the minimum steps from the start node to any of end node,
        // including the total number of navigation steps
        val lcmElements = listOf(navigation.size.toLong()) + eachStartToAnEndSteps.map(Pair<NodeName, Long>::second)
        return lcmElements.fold(1) { acc, n -> lcm(acc, n) }
    }

    private fun lcm(a: Long, b: Long): Long {
        // It is known that: gcd(a, b) * lcm(a, b) = a * b
        // Therefore: lcm(a, b) = (a * b) / gcd(a, b)
        return (a * b) / gcd(a, b)
    }

    private fun gcd(a: Long, b: Long): Long {
        // Calculating the Greatest Common Divisor with Euclidean Algorithm
        var _a = a
        var _b = b
        var r: Long
        while(_b != 0L) {
            r = _a % _b
            _a = _b
            _b = r
        }
        return _a
    }

    companion object {
        fun parse(input: List<String>): Chart {
            return Chart(
                navigation = input[0].toCharArray().map(Direction::parse).toList(),
                network = input.subList(2, input.size).map(Node::parse).associateBy(Node::name)
            )
        }
    }
}

enum class Direction {
    LEFT, RIGHT;

    companion object {
        fun parse(input: Char): Direction = when (input) {
            'L' -> LEFT
            'R' -> RIGHT
            else -> throw IllegalArgumentException("Illegal input $input received to parse DIRECTION")
        }
    }
}

data class NodeName(val value: String) {
    fun isStartNode(): Boolean = value.endsWith("A")
    fun isEndNode(): Boolean = value.endsWith("Z")
    override fun toString(): String = value
}

data class Node(
    val name: NodeName,
    val left: NodeName,
    val right: NodeName
) {
    companion object {
        fun parse(input: String): Node {
            val regex = Regex("(?<nodeName>[A-Z0-9]{3}) = \\((?<left>[A-Z0-9]{3}), (?<right>[A-Z0-9]{3})\\)")

            val match =
                regex.find(input) ?: throw IllegalArgumentException("Could not find matches for node regex on $input")

            return Node(
                name = NodeName(match.groups["nodeName"]!!.value),
                left = NodeName(match.groups["left"]!!.value),
                right = NodeName(match.groups["right"]!!.value)
            )
        }
    }
}