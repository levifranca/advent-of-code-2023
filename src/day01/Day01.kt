package day01

import println
import readInput

fun main() {
    fun part1(input: List<String>): Int {
        val calValues = mutableListOf<Int>()
        input.forEach { line ->
            val lineChars = line.toCharArray()
            var firstDigit: Char? = null
            var lastDigit: Char? = null
            for (i in 0..lineChars.lastIndex) {
                val char = lineChars[i]
                if (char.isDigit()) {
                    if (firstDigit == null) {
                        firstDigit = char
                    }
                    lastDigit = char
                }
            }
            val calValue = (firstDigit.toString() + lastDigit).toInt()
            calValues.add(calValue)
        }
        return calValues.sum()
    }

    fun part2(input: List<String>): Int {
        val calValues = mutableListOf<Int>()
        input.forEach { line ->
            val lineChars = line.toCharArray()
            val letters = mutableListOf<Char>()
            val lettersReversed = mutableListOf<Char>()
            var firstDigit: Int? = null
            var lastDigit: Int? = null
            for (i in 0..lineChars.lastIndex) {
                val j = lineChars.lastIndex - i
                val charFront = lineChars[i]
                val charBack = lineChars[j]

                // Finding First
                if (firstDigit == null) {
                    if (charFront.isDigit()) {
                        firstDigit = charFront.digitToInt()
                    } else {
                        letters.add(charFront)
                        val digitFromLetters = letters.asString().asDigit()
                        if (digitFromLetters != null) {
                            firstDigit = digitFromLetters
                        }
                    }
                }

                // Finding Last
                if (lastDigit == null) {
                    if (charBack.isDigit()) {
                        lastDigit = charBack.digitToInt()
                    } else {
                        lettersReversed.add(charBack)
                        val digitFromLettersReversed = lettersReversed.asReversedString().asDigit()
                        if (digitFromLettersReversed != null) {
                            lastDigit = digitFromLettersReversed
                        }
                    }
                }
            }
            val calValue = (firstDigit!! * 10) + lastDigit!!
            calValues.add(calValue)
        }
        return calValues.sum()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("day01/Day01_test")
    check(part1(testInput) == 142)
    val testInput2 = readInput("day01/Day01_test2")
    check(part2(testInput2) == 281)

    val input = readInput("day01/Day01")
    part1(input).println()
    part2(input).println()
}

private fun String.asDigit(): Int? {
    if (this.startsWith("one") || this.endsWith("one")) return 1
    if (this.startsWith("two") || this.endsWith("two")) return 2
    if (this.startsWith("three") || this.endsWith("three")) return 3
    if (this.startsWith("four") || this.endsWith("four")) return 4
    if (this.startsWith("five") || this.endsWith("five")) return 5
    if (this.startsWith("six") || this.endsWith("six")) return 6
    if (this.startsWith("seven") || this.endsWith("seven")) return 7
    if (this.startsWith("eight") || this.endsWith("eight")) return 8
    if (this.startsWith("nine") || this.endsWith("nine")) return 9
    return null
}

private fun <E> List<E>.asString(): String = this.fold("") { acc, it -> acc + it }
private fun <E> List<E>.asReversedString(): String = this.fold("") { acc, it -> it.toString() + acc }
