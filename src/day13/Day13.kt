package day13

import println
import readInput

fun main() {
    fun part1(input: List<String>): Int {
        return Field.parse(input).patterns.sumOf { it.summary() }
    }

    fun part2(input: List<String>): Int {
        return Field.parse(input).patterns.sumOf { it.summaryWithSmudge() }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("day13/Day13_test")
    check(part1(testInput) == 405)
    check(part2(testInput) == 400)

    val testInput2 = readInput("day13/Day13_test2")
    check(part1(testInput2) == 3109)

    val testInput3 = readInput("day13/Day13_test3")
    check(part1(testInput3) == 1200)


    val input = readInput("day13/Day13")
    part1(input).println()
    part2(input).println()
}

class Field(
    val patterns: List<Pattern>
) {
    companion object {
        fun parse(input: List<String>): Field {
            val patterns = mutableListOf<Pattern>()
            val lines = mutableListOf<String>()
            input.forEach { line ->
                if (line.isBlank()) {
                    patterns.add(Pattern.parse(lines))
                    lines.clear()
                } else {
                    lines.add(line)
                }
            }
            if (lines.isNotEmpty()) {
                patterns.add(Pattern.parse(lines))
            }
            return Field(patterns)
        }
    }
}

class Pattern(
    val matrix: Array<CharArray>
) {
    fun summary(): Int {
        // check vertical reflection
        var reflectionLineIdx = findReflectionLineIndex(matrix.transpose())
        if (reflectionLineIdx != null) {
            return reflectionLineIdx + 1
        }

        // check horizontal reflection
        reflectionLineIdx = findReflectionLineIndex(matrix)
        if (reflectionLineIdx != null) {
            return (reflectionLineIdx + 1) * 100
        }

        throw IllegalStateException("There is no reflection")
    }

    private fun findReflectionLineIndex(
        matrix: Array<CharArray>
    ): Int? {
        var reflectionLineIdx = 0
        while (reflectionLineIdx < matrix.size-1) {
            val pairs = generatePairs(reflectionLineIdx).filter { it.second < matrix.size }
            val foundReflection = pairs.all { pair -> matrix[pair.first].contentEquals(matrix[pair.second]) }
            if (foundReflection) {
                return reflectionLineIdx
            }
            reflectionLineIdx++
        }
        return null
    }

    private fun generatePairs(index: Int): List<Pair<Int, Int>> =
        (0..index).map { i -> Pair(i, ((2 * index + 1) - i)) }

    fun summaryWithSmudge(): Int {
        // check vertical reflection
        var reflectionLineIdx = findReflectionLineIndexWithSmudge(matrix.transpose())
        if (reflectionLineIdx != null) {
            return reflectionLineIdx + 1
        }

        // check horizontal reflection
        reflectionLineIdx = findReflectionLineIndexWithSmudge(matrix)
        if (reflectionLineIdx != null) {
            return (reflectionLineIdx + 1) * 100
        }

        throw IllegalStateException("There is no reflection")
    }

    private fun findReflectionLineIndexWithSmudge(
        matrix: Array<CharArray>
    ): Int? {
        var reflectionLineIdx = 0
        while (reflectionLineIdx < matrix.size-1) {
            val pairs = generatePairs(reflectionLineIdx).filter { it.second < matrix.size }
            val diffCount = pairs.sumOf { pair -> diffCount(matrix[pair.first], matrix[pair.second]) }
            if (diffCount == 1) {
                // If there is only a single difference than a single smudge fix will fix the reflection
                return reflectionLineIdx
            }
            reflectionLineIdx++
        }
        return null
    }

    private fun diffCount(array1: CharArray, array2: CharArray): Int {
        if (array1.size != array2.size) {
            throw IllegalArgumentException(
                "Received arrays of different size to count diff: ${array1.concatToString()} ${array2.concatToString()}"
            )
        }

        var diffCount = 0
        for(i in array1.indices) {
            if (array1[i] != array2[i]) {
                diffCount++
            }
        }
        return diffCount
    }

    companion object {
        fun parse(input: List<String>): Pattern {
            return Pattern(
                matrix = input.map { it.toCharArray() }.toTypedArray()
            )
        }
    }
}

fun Array<CharArray>.transpose(): Array<CharArray> {
    val colSize = this[0].size
    val output: Array<CharArray> = Array(colSize) { CharArray(this.size) }
    (0 until colSize).forEach { col ->
        this.forEachIndexed { lineIdx, line ->
            output[col][lineIdx] = line[col]
        }
    }
    return output
}

