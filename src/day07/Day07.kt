package day07

import println
import readInput
import java.util.Comparator

fun main() {
    fun part1(input: List<String>): Int {
        val hands = input.map(Part1.Hand::parse)
        return hands.sortedWith(Part1.HandComparator()).foldIndexed(0) { i, acc, hand -> acc + (hand.bid * (i + 1)) }
    }

    fun part2(input: List<String>): Int {
        val hands = input.map(Part2.Hand::parse)
        return hands.sortedWith(Part2.HandComparator()).foldIndexed(0) { i, acc, hand -> acc + (hand.bid * (i + 1)) }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("day07/Day07_test")
    check(part1(testInput) == 6440)
    check(part2(testInput) == 5905)

    val input = readInput("day07/Day07")
    part1(input).println()
    part2(input).println()
}

enum class CardType(
    private val strength: Int
) {
    FIVE_OF_A_KIND(6),
    FOUR_OF_A_KIND(5),
    FULL_HOUSE(4),
    THREE_OF_A_KIND(3),
    TWO_PAIRS(2),
    ONE_PAIR(1),
    HIGH_CARD(0);

    operator fun minus(cardType: CardType) = this.strength - cardType.strength
}

class Part1 {
    enum class Card(
        private val value: Int,
        private val label: Char
    ) {
        A(12, 'A'), K(11, 'K'), Q(10, 'Q'),
        J(9, 'J'), T(8, 'T'), _9(7, '9'),
        _8(6, '8'), _7(5, '7'), _6(4, '6'),
        _5(3, '5'), _4(2, '4'), _3(1, '3'),
        _2(0, '2');

        operator fun minus(card: Card) = this.value - card.value

        companion object {
            fun parse(input: Char): Card {
                return entries.find { it.label == input }!!
            }
        }

    }

    data class Hand(
        val firstCard: Card,
        val secondCard: Card,
        val thirdCard: Card,
        val fourthCard: Card,
        val fifthCard: Card,
        val bid: Int
    ) {

        private val cardsMap = mutableMapOf<Card, Int>()


        init {
            cardsMap[firstCard] = cardsMap.getOrDefault(firstCard, 0) + 1
            cardsMap[secondCard] = cardsMap.getOrDefault(secondCard, 0) + 1
            cardsMap[thirdCard] = cardsMap.getOrDefault(thirdCard, 0) + 1
            cardsMap[fourthCard] = cardsMap.getOrDefault(fourthCard, 0) + 1
            cardsMap[fifthCard] = cardsMap.getOrDefault(fifthCard, 0) + 1
        }

        fun type(): CardType = when (cardsMap.size) {
            1 -> CardType.FIVE_OF_A_KIND
            2 -> when {
                cardsMap.values.any { it == 4 } -> CardType.FOUR_OF_A_KIND
                else -> CardType.FULL_HOUSE
            }

            3 -> when {
                cardsMap.values.any { it == 3 } -> CardType.THREE_OF_A_KIND
                else -> CardType.TWO_PAIRS
            }

            4 -> CardType.ONE_PAIR
            else -> CardType.HIGH_CARD
        }

        companion object {
            fun parse(input: String): Hand {
                val split = input.split(" ")
                return Hand(
                    firstCard = Card.parse(split[0][0]),
                    secondCard = Card.parse(split[0][1]),
                    thirdCard = Card.parse(split[0][2]),
                    fourthCard = Card.parse(split[0][3]),
                    fifthCard = Card.parse(split[0][4]),
                    bid = split[1].toInt()
                )
            }
        }

    }

    class HandComparator : Comparator<Hand> {
        override fun compare(hand: Hand, otherHand: Hand): Int {
            val typeCompare = hand.type() - otherHand.type()
            if (typeCompare != 0) {
                return typeCompare
            }
            val firstCardCompare = hand.firstCard - otherHand.firstCard
            if (firstCardCompare != 0) {
                return firstCardCompare
            }

            val secondCardCompare = hand.secondCard - otherHand.secondCard
            if (secondCardCompare != 0) {
                return secondCardCompare
            }

            val thirdCardCompare = hand.thirdCard - otherHand.thirdCard
            if (thirdCardCompare != 0) {
                return thirdCardCompare
            }

            val fourthCardCompare = hand.fourthCard - otherHand.fourthCard
            if (fourthCardCompare != 0) {
                return fourthCardCompare
            }

            val fifthCardCompare = hand.fifthCard - otherHand.fifthCard
            if (fifthCardCompare != 0) {
                return fifthCardCompare
            }
            return 0
        }
    }
}

class Part2 {
    enum class Card(
        private val value: Int,
        private val label: Char
    ) {
        A(12, 'A'), K(11, 'K'), Q(10, 'Q'),
        T(9, 'T'), _9(8, '9'), _8(7, '8'),
        _7(6, '7'), _6(5, '6'), _5(4, '5'),
        _4(3, '4'), _3(2, '3'), _2(1, '2'),
        J(0, 'J');

        operator fun minus(card: Card) = this.value - card.value

        companion object {
            fun parse(input: Char): Card {
                return entries.find { it.label == input }!!
            }
        }

    }

    data class Hand(
        val firstCard: Card,
        val secondCard: Card,
        val thirdCard: Card,
        val fourthCard: Card,
        val fifthCard: Card,
        val bid: Int
    ) {

        private val cardsMap = mutableMapOf<Card, Int>()
        private var numberOfJokers = 0

        init {
            if (firstCard == Card.J) {
                numberOfJokers++
            } else {
                cardsMap[firstCard] = cardsMap.getOrDefault(firstCard, 0) + 1
            }
            if (secondCard == Card.J) {
                numberOfJokers++
            } else {
                cardsMap[secondCard] = cardsMap.getOrDefault(secondCard, 0) + 1
            }
            if (thirdCard == Card.J) {
                numberOfJokers++
            } else {
                cardsMap[thirdCard] = cardsMap.getOrDefault(thirdCard, 0) + 1
            }
            if (fourthCard == Card.J) {
                numberOfJokers++
            } else {
                cardsMap[fourthCard] = cardsMap.getOrDefault(fourthCard, 0) + 1
            }
            if (fifthCard == Card.J) {
                numberOfJokers++
            } else {
                cardsMap[fifthCard] = cardsMap.getOrDefault(fifthCard, 0) + 1
            }
        }

        fun type(): CardType = when (numberOfJokers) {
            0 -> zeroJokers()
            1 -> oneJoker()
            2 -> twoJokers()
            3 -> threeJokers()
            else -> CardType.FIVE_OF_A_KIND // 4 or 5 jokers are always FIVE_OF_A_KIND
        }

        private fun zeroJokers(): CardType = when (cardsMap.size) {
            1 -> CardType.FIVE_OF_A_KIND
            2 -> when {
                cardsMap.values.any { it == 4 } -> CardType.FOUR_OF_A_KIND
                else -> CardType.FULL_HOUSE
            }

            3 -> when {
                cardsMap.values.any { it == 3 } -> CardType.THREE_OF_A_KIND
                else -> CardType.TWO_PAIRS
            }

            4 -> CardType.ONE_PAIR
            else -> CardType.HIGH_CARD
        }

        private fun oneJoker(): CardType = when (cardsMap.size) {
            1 -> CardType.FIVE_OF_A_KIND
            2 -> when {
                cardsMap.values.any { it == 3 } -> CardType.FOUR_OF_A_KIND
                else -> CardType.FULL_HOUSE
            }

            3 -> CardType.THREE_OF_A_KIND
            4 -> CardType.ONE_PAIR
            else -> throw IllegalStateException("One Joker and ${cardsMap.size} different cards ???")
        }

        private fun twoJokers(): CardType = when (cardsMap.size) {
            1 -> CardType.FIVE_OF_A_KIND
            2 -> CardType.FOUR_OF_A_KIND
            3 -> CardType.THREE_OF_A_KIND
            else -> throw IllegalStateException("Two Joker and ${cardsMap.size} different cards ???")
        }

        private fun threeJokers(): CardType = when (cardsMap.size) {
            1 -> CardType.FIVE_OF_A_KIND
            2 -> CardType.FOUR_OF_A_KIND
            else -> throw IllegalStateException("Three Jokers and ${cardsMap.size} different cards ???")
        }

        companion object {
            fun parse(input: String): Hand {
                val split = input.split(" ")
                return Hand(
                    firstCard = Card.parse(split[0][0]),
                    secondCard = Card.parse(split[0][1]),
                    thirdCard = Card.parse(split[0][2]),
                    fourthCard = Card.parse(split[0][3]),
                    fifthCard = Card.parse(split[0][4]),
                    bid = split[1].toInt()
                )
            }
        }

    }

    class HandComparator : Comparator<Hand> {
        override fun compare(hand: Hand, otherHand: Hand): Int {
            val typeCompare = hand.type() - otherHand.type()
            if (typeCompare != 0) {
                return typeCompare
            }
            val firstCardCompare = hand.firstCard - otherHand.firstCard
            if (firstCardCompare != 0) {
                return firstCardCompare
            }

            val secondCardCompare = hand.secondCard - otherHand.secondCard
            if (secondCardCompare != 0) {
                return secondCardCompare
            }

            val thirdCardCompare = hand.thirdCard - otherHand.thirdCard
            if (thirdCardCompare != 0) {
                return thirdCardCompare
            }

            val fourthCardCompare = hand.fourthCard - otherHand.fourthCard
            if (fourthCardCompare != 0) {
                return fourthCardCompare
            }

            val fifthCardCompare = hand.fifthCard - otherHand.fifthCard
            if (fifthCardCompare != 0) {
                return fifthCardCompare
            }
            return 0
        }
    }
}

