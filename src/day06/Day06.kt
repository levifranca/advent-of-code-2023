package day06

import println
import readInput

fun main() {
    fun part1(input: List<String>): Long {
        val timeArray: Array<Long> = input[0].removePrefix("Time:")
            .split(" ").filter(String::isNotBlank).map(String::toLong).toTypedArray()
        val distanceArray: Array<Long> = input[1].removePrefix("Distance:")
            .split(" ").filter(String::isNotBlank).map(String::toLong).toTypedArray()

        val races: List<Race> = timeArray.zip(distanceArray).map { (time, distance) -> Race(time, distance) }

        return races.map(Race::waysToWin).reduce { acc, i -> acc * i }
    }

    fun part2(input: List<String>): Long {
        val time: Long = input[0].removePrefix("Time:")
            .split(" ").filter(String::isNotBlank).joinToString("").toLong()
        val distance: Long = input[1].removePrefix("Distance:")
            .split(" ").filter(String::isNotBlank).joinToString("").toLong()

        return Race(time, distance).waysToWin()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("day06/Day06_test")
    check(part1(testInput) == 288L)
    check(part2(testInput) == 71503L)

    val input = readInput("day06/Day06")
    part1(input).println()
    part2(input).println()
}

data class Race(
    private val time: Long,
    private val distance: Long
) {
    fun waysToWin(): Long {
        var waysToWinCounter = 0L
        (0..time).forEach { timeHoldingButton ->
            val travelledDistance = (time - timeHoldingButton) * timeHoldingButton
            if (distance < travelledDistance) {
                waysToWinCounter++
            }
        }

        return waysToWinCounter
    }
}