package day14

import println
import readInput
import java.time.Instant

fun main() {
    fun part1(input: List<String>): Int {
        return Platform.parse(input).tiltNorth().load().also { it.println() }
    }

    fun part2(input: List<String>): Int {
        return Platform.parse(input).spinCycle(1_000_000_000).load().also { it.println() }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("day14/Day14_test")
    check(part1(testInput) == 136)
    check(part2(testInput) == 64)

    val input = readInput("day14/Day14")
    part1(input).println()
    part2(input).println()
}

data class Platform(
    private val matrix: List<String>
) {

    fun tiltNorth(): Platform {
        return matrix
            .transpose()
            .map { it.rollRocks() }
            .transpose()
            .let { Platform(it) }
    }

    fun tiltWest(): Platform {
        return matrix
            .map { it.rollRocks() }
            .let { Platform(it) }
    }

    fun tiltSouth(): Platform {
        return matrix
            .transpose()
            .map { it.rollRocks(reversed = true) }
            .transpose()
            .let { Platform(it) }
    }

    fun tiltEast(): Platform {
        return matrix
            .map { it.rollRocks(reversed = true) }
            .let { Platform(it) }
    }

    fun spinCycle(times: Int): Platform {
        var result = this
        for (i in (0 until times)) {
            result = this
                .tiltNorth()
                .tiltWest()
                .tiltSouth()
                .tiltEast()
            if (i % 10_000_000 == 0) {
                "${Instant.now()} - Cycle ${i + 1}".println()
            }
        }
        return result
    }

    fun load(): Int {
        return matrix
            .mapIndexed { i, line -> (matrix.size - i) * line.count { it == 'O' } }
            .sum()
    }

    fun print() {
        matrix.forEach { it.println() }
    }

    companion object {
        fun parse(input: List<String>): Platform {
            return Platform(input)
        }
    }
}


private val cache = mutableMapOf<Pair<String, Boolean>, String>()
private fun String.rollRocks(reversed: Boolean = false): String {
    val key = Pair(this, reversed)
    val memo = cache[key]
    if (memo != null) {
        //"Found memo'ed $key = $memo".println()
        return memo
    }

    val splits = this.split('#')
    val segments = mutableListOf<String>()
    for (str in splits) {
        val countRocks = str.count { it == 'O' }
        val newStr = if (reversed) {
            ((0 until (str.length - countRocks)).map { "." }+ (0 until countRocks).map { "O" }).joinToString("")
        } else {
            ((0 until countRocks).map { "O" } + (0 until (str.length - countRocks)).map { "." }).joinToString("")
        }
        segments.add(newStr)
    }
    val result = segments.joinToString("#")

    //"Memo'ing $key = $result".println()
    cache[key] = result
    return result
}

fun List<String>.transpose(): List<String> {
    val colSize = this[0].length
    val output = mutableListOf(*(0 until colSize).map { "" }.toTypedArray())
    (0 until colSize).forEach { col ->
        this.forEach { line ->
            output[col] = output.getOrElse(col) { _ -> "" } + line[col]
        }
    }
    return output
}