package day02

import println
import readInput

fun main() {
    fun part1(input: List<String>): Int {
        val games = input.map(Game::parse)

        val bagConstraint = Constraint(redQty = 12, greenQty = 13, blueQty = 14)
        val possibleGames = games.filter { it.isPossible(bagConstraint) }
        return possibleGames.sumOf { it.id }
    }

    fun part2(input: List<String>): Int {

        val games = input.map(Game::parse)

        return games.sumOf { it.powerOfMinimumPossibleConstraint() }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("day02/Day02_test")
    check(part1(testInput) == 8)
    check(part2(testInput) == 2286)

    val input = readInput("day02/Day02")
    part1(input).println()
    part2(input).println()
}

data class Constraint(val redQty: Int, val greenQty: Int, val blueQty: Int) {
    val power = redQty * greenQty * blueQty
}

private data class Game private constructor(
        val id: Int,
        val cubeArrangements: List<CubeArrangement>
) {
    fun isPossible(constraint: Constraint): Boolean = cubeArrangements.all { it.isPossible(constraint) }

    fun powerOfMinimumPossibleConstraint(): Int {
        return getMinimumPossibleConstraint().power
    }

    private fun getMinimumPossibleConstraint(): Constraint {
        var maxRedQty = 0
        var maxGreenQty = 0
        var maxBlueQty = 0

        cubeArrangements.forEach { cubeArrangement ->
            if (cubeArrangement.redCubesQty > maxRedQty) {
                maxRedQty = cubeArrangement.redCubesQty
            }
            if (cubeArrangement.greenCubesQty > maxGreenQty) {
                maxGreenQty = cubeArrangement.greenCubesQty
            }
            if (cubeArrangement.blueCubesQty > maxBlueQty) {
                maxBlueQty = cubeArrangement.blueCubesQty
            }
        }

        return Constraint(
                redQty = maxRedQty,
                greenQty = maxGreenQty,
                blueQty = maxBlueQty
        )
    }

    companion object {
        fun parse(input: String): Game {
            // input pattern is:
            // Game {id}: {cube arrangement set 1}; {cube set 2}; {cube set 3}; [...]
            val gameLineParts = input.split(":")
            return Game(
                    id = gameLineParts[0].split(" ")[1].toInt(),
                    cubeArrangements = gameLineParts[1].trim().split(";").map(CubeArrangement::parse)
            )
        }
    }
}

private data class CubeArrangement private constructor(
        val redCubesQty: Int,
        val greenCubesQty: Int,
        val blueCubesQty: Int
) {
    fun isPossible(
            constraint: Constraint
    ): Boolean = redCubesQty <= constraint.redQty
            && greenCubesQty <= constraint.greenQty
            && blueCubesQty <= constraint.blueQty

    companion object {
        fun parse(input: String): CubeArrangement {
            // input pattern is:
            // ({qty} red) or ({qty} green) or ({qty} blue), up to 3 times
            val qtyColorStrings = input.split(",")
            var redQty = 0
            var greenQty = 0
            var blueQty = 0
            qtyColorStrings.forEach { qtyColorString ->
                val qrtColorList = qtyColorString.trim().split(" ")
                when (qrtColorList[1]) {
                    "red" -> redQty = qrtColorList[0].toInt()
                    "green" -> greenQty = qrtColorList[0].toInt()
                    "blue" -> blueQty = qrtColorList[0].toInt()
                    else -> throw IllegalArgumentException("Unexpected qty color string to parse: $qtyColorString")
                }
            }

            return CubeArrangement(
                    redQty,
                    greenQty,
                    blueQty
            )
        }
    }
}