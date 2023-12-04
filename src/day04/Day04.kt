package day04

import println
import readInput
import kotlin.math.pow

fun main() {
    fun part1(input: List<String>): Int {
        val cards = input.map(Card::parse)
        return cards.sumOf { it.points() }
    }

    fun part2(input: List<String>): Int {
        val cards = input.map(Card::parse)
        val cardsInstancesMap = Array(cards.size) { 1 }
        var totalCardsInstance = cards.size
        cards.forEachIndexed { cardPos, card ->
            val matchingNumbersCount = card.matchingNumbersCount
            (cardPos + 1..cardPos + matchingNumbersCount).forEach { followingCardPos ->
                if (followingCardPos >= cardsInstancesMap.size) {
                    return@forEach
                }
                cardsInstancesMap[followingCardPos] += cardsInstancesMap[cardPos]
                totalCardsInstance += cardsInstancesMap[cardPos]
            }
        }

        return totalCardsInstance
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("day04/Day04_test")
    check(part1(testInput) == 13)
    val testInput2 = readInput("day04/Day04_test")
    check(part2(testInput2) == 30)

    val input = readInput("day04/Day04")
    part1(input).println()
    part2(input).println()
}

class Card private constructor(
    winningNumbers: Set<Int>,
    numbersYouHave: List<Int>
) {
    val matchingNumbersCount: Int = numbersYouHave.intersect(winningNumbers).size

    fun points(): Int {
        if (matchingNumbersCount == 0) {
            return 0
        }
        return 2.0.pow(matchingNumbersCount - 1).toInt()
    }

    companion object {
        fun parse(input: String): Card {
            // input is like Card 1: 41 48 83 86 17 | 83 86  6 31 17  9 48 53
            val numbersPart = input.split(":")[1]
            val numbersSplit = numbersPart.split("|")
            val winningNumbers = numbersSplit[0].trim().split(" ").filter(String::isNotBlank).map(String::toInt).toSet()
            val numbersYouHave = numbersSplit[1].trim().split(" ").filter(String::isNotBlank).map(String::toInt)
            return Card(winningNumbers, numbersYouHave)
        }
    }
}
