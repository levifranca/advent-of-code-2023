package dayXX

import println
import readInput

fun main() {
    fun part1(input: List<String>): Int {
        return input.size
    }

    fun part2(input: List<String>): Int {
        return input.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("dayXX/DayXX_test")
    check(part1(testInput) == 0)
    val testInput2 = readInput("dayXX/DayXX_test")
    check(part2(testInput2) == 0)

    val input = readInput("dayXX/DayXX")
    part1(input).println()
    part2(input).println()
}
