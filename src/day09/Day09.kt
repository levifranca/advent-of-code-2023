package day09

import println
import readInput

fun main() {
    fun part1(input: List<String>): Int {
        return Oasis.parse(input).extrapolatedValues().sum()
    }

    fun part2(input: List<String>): Int {
        return Oasis.parse(input).backwardExtrapolatedValues().sum()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("day09/Day09_test")
    check(part1(testInput) == 114)
    check(part2(testInput) == 2)

    val input = readInput("day09/Day09")
    part1(input).println()
    part2(input).println()
}

data class Oasis(
    private val valuesHistory: List<List<Int>>
) {
    fun extrapolatedValues(): List<Int> {
        return valuesHistory.map(this::getExtrapolatedValue)
    }

    fun backwardExtrapolatedValues(): List<Int> {
        return valuesHistory.map(this::getBackwardExtrapolatedValue)
    }

    private fun getExtrapolatedValue(history: List<Int>): Int {
        if (history.all { it == 0 }) {
            return 0
        }
        val diffs = history.zipWithNext { a, b -> b-a }
        return getExtrapolatedValue(diffs) + history.last()
    }

    private fun getBackwardExtrapolatedValue(history: List<Int>): Int {
        if (history.all { it == 0 }) {
            return 0
        }
        val diffs = history.zipWithNext { a, b -> b-a }
        return history.first() - getBackwardExtrapolatedValue(diffs)
    }


    companion object {
        fun parse(input: List<String>): Oasis {
            return Oasis(
                valuesHistory = input.map { line -> line.split(" ").map(String::toInt) }
            )
        }
    }
}