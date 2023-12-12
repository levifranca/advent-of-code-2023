package day12

import println
import readInput
import kotlin.math.pow

fun main() {
    fun part1(input: List<String>): Int {
        return SpringsReport.parse(input).arrangements().sumOf { it.count() }
    }

    fun part2(input: List<String>): Int {
        return SpringsReport.parse(input).unfoldedArrangements().sumOf { it.count() }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("day12/Day12_test")
    check(part1(testInput) == 21)
    check(part2(testInput) == 525152)

    val input = readInput("day12/Day12")
    part1(input).println()
    part2(input).println()
}

private class SpringsReport(
    private val springsRows: List<RowRecord>
) {

    fun arrangements(): List<List<CharArray>> = springsRows.map(RowRecord::possibleArrangements)

    fun unfoldedArrangements(): List<List<CharArray>> = springsRows
        .asSequence()
        .map(RowRecord::unfold)
        .map(RowRecord::possibleArrangements)
        .toList()

    companion object {
        fun parse(input: List<String>): SpringsReport {
            return SpringsReport(input.map { line ->
                val split = line.split(" ")
                RowRecord(
                    springs = split[0].toCharArray(),
                    groupsOfDamagedSprings = split[1].split(",").map { it.toInt() }
                )
            })
        }
    }
}

private data class RowRecord(
    val springs: CharArray,
    val groupsOfDamagedSprings: List<Int>
) {

    companion object {
        private val damagedGroupRegex = Regex("(#)+")
    }

    fun possibleArrangements(): List<CharArray> {
        val possibleArrangements = mutableListOf<CharArray>()
        val unknownIndices: List<Int> = springs.mapIndexed { i, char ->
            if (char == '?') {
                i
            } else {
                null
            }
        }.filterNotNull()

        var boolArray = BooleanArray(unknownIndices.size)
        val allCombinations = 2.0F.pow(unknownIndices.size).toLong()
        val allCombinationsRange = (0..<allCombinations)

        allCombinationsRange
            .asSequence()
            .forEach { _ ->
                val arrangement = springs.copyOf()
                for (i in boolArray.indices) {
                    if (boolArray[i]) {
                        arrangement[unknownIndices[i]] = '#'
                    } else {
                        arrangement[unknownIndices[i]] = '.'
                    }
                }
                if (isPossible(arrangement)) {
                    possibleArrangements.add(arrangement)
                }
                boolArray++
            }

        return possibleArrangements
    }

    private fun isPossible(arrangement: CharArray): Boolean {
        if (arrangement.contains('?')) {
            throw IllegalStateException("arrangement should not contain ?")
        }
        val matches = damagedGroupRegex.findAll(arrangement.concatToString())
        val foundGroups = matches.map { it.value.length }.toList()
        return foundGroups == groupsOfDamagedSprings
    }

    fun unfold(): RowRecord {
        val unfoldedSprings = (1..5).joinToString("?") { springs.concatToString() }.toCharArray()
        val unfoldedGroups = (1..5).flatMap { groupsOfDamagedSprings }
        return RowRecord(
            springs = unfoldedSprings,
            groupsOfDamagedSprings = unfoldedGroups
        )
    }
}

private operator fun BooleanArray.inc(): BooleanArray {
    for (i in indices.reversed()) {
        if (this[i]) {
            this[i] = false
        } else {
            this[i] = true
            break
        }
    }
    return this
}
