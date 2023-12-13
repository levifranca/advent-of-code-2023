package day12

import println
import readInput
import kotlin.math.pow

fun main() {
    fun part1(input: List<String>): Int {
        return SpringsReport.parse(input).arrangementsCounts().sum()
    }

    fun part2(input: List<String>): Int {
        return SpringsReport.parse(input).unfoldedArrangementsCounts().sum()
    }

//    check(recurse("??", listOf(1)).also { it.println() } == 2)
//    check(recurse("?#??##?", listOf(2, 3)).also { it.println() } == 3)
//    check(recurse("#??##?", listOf(1, 3)).also { it.println() } == 2)
//    check(recurse("#??##?", listOf(2, 3)) == 1)

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("day12/Day12_test")
    check(part1(testInput) == 21)
    check(part2(testInput) == 525152)

    val input = readInput("day12/Day12")
    part1(input).println()
    //part2(input).println()
}

private class SpringsReport(
    private val springsRows: List<RowRecord>
) {

    fun arrangements(): List<List<CharArray>> = springsRows.map(RowRecord::possibleArrangements)
    fun arrangementsCounts(): List<Int> = springsRows.map(RowRecord::possibleArrangementCount)

    fun unfoldedArrangementsCounts(): List<Int> = springsRows
        .asSequence()
        .map(RowRecord::unfold)
        .map(RowRecord::possibleArrangementCount)
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

    fun possibleArrangementCount(): Int =
        recurse(springs.concatToString(), groupsOfDamagedSprings)
    //.also { it.println() }


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

fun recurse(springs: String, groupsOfDamagedSprings: List<Int>, depth: Int = 0): Int {
    if (springs.isEmpty()) {
        if (groupsOfDamagedSprings.isEmpty()) {
            //println("${(0..depth).joinToString("") { "\t" }}recurse $springs $groupsOfDamagedSprings = 1")
            return 1
        }
        //println("${(0..depth).joinToString("") { "\t" }}recurse $springs $groupsOfDamagedSprings = 0")
        return 0
    }
    when (springs[0]) {
        '.' -> {
            val result = recurse(springs.substring(1, springs.length), groupsOfDamagedSprings, depth + 1)
            //println("${(0..depth).joinToString("") { "\t" }}recurse $springs $groupsOfDamagedSprings = $result")
            return result
        }

        '#' -> {
            if (groupsOfDamagedSprings.isEmpty()) {
                // found damaged but the groups are over. Invalid arrangement
                //println("${(0..depth).joinToString("") { "\t" }}recurse $springs $groupsOfDamagedSprings = 0")
                return 0
            }

            val groupSize = groupsOfDamagedSprings[0]
            if (springs.length < groupSize) {
                // invalid group found
                //println("${(0..depth).joinToString("") { "\t" }}recurse $springs $groupsOfDamagedSprings = 0")
                return 0
            }
            val group = springs.substring(0, groupSize)
            if (group.contains('.')) {
                // invalid group found
                //println("${(0..depth).joinToString("") { "\t" }}recurse $springs $groupsOfDamagedSprings = 0")
                return 0
            }

            if (springs.length == groupSize) {
                // recurse one last time with empty string
                val result = recurse(
                    "",
                    groupsOfDamagedSprings.subList(1, groupsOfDamagedSprings.size),
                    depth + 1
                )
                //println("${(0..depth).joinToString("") { "\t" }}recurse $springs $groupsOfDamagedSprings = $result")
                return result
            }

            val charAfterGroup = springs[groupSize]
            when (charAfterGroup) {
                '#' -> {
                    // invalid group is larger than expected
                    //println("${(0..depth).joinToString("") { "\t" }}recurse $springs $groupsOfDamagedSprings = 0")
                    return 0
                }

                '?',
                '.' -> {
                    // non-last group found successfully, recurse with the rest of the string
                    val result = recurse(
                        springs.substring(groupSize + 1),
                        groupsOfDamagedSprings.subList(1, groupsOfDamagedSprings.size),
                        depth + 1
                    )
                    //println("${(0..depth).joinToString("") { "\t" }}recurse $springs $groupsOfDamagedSprings = $result")
                    return result
                }

                else -> throw IllegalStateException("WTF? $springs $groupsOfDamagedSprings")
            }
        }

        '?' -> {
            val result = recurse('.' + springs.substring(1, springs.length), groupsOfDamagedSprings, depth + 1) +
                    recurse('#' + springs.substring(1, springs.length), groupsOfDamagedSprings, depth + 1)
            //println("${(0..depth).joinToString("") { "\t" }}recurse $springs $groupsOfDamagedSprings = $result")
            return result
        }

        else -> throw IllegalStateException("Char at $springs [0] is invalid: ${springs[0]}")
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
