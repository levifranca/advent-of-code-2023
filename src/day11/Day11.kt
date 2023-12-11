package day11

import println
import readInput

fun main() {
    fun part1(input: List<String>): Long {
        return SpaceImage.parse(input).galaxiesPairs().sumOf { it.pathLength }
    }

    fun part2(input: List<String>, expansionDegree: Int): Long {
        return SpaceImage.parse(input).galaxiesPairs(expansionDegree).sumOf { it.pathLength }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("day11/Day11_test")
    check(part1(testInput) == 374L)
    check(part2(testInput, 10) == 1030L)
    check(part2(testInput, 100) == 8410L)

    val input = readInput("day11/Day11")
    part1(input).println()
    part2(input, 1000000).println()
}

private class SpaceImage(
    private val galaxies: List<Galaxy>,
    private val rowsWithoutGalaxy: Set<Int>,
    private val columnsWithoutGalaxy: Set<Int>
) {
    fun galaxiesPairs(
        expansionDegree: Int = 2 // how many each empty row/column is worth
    ): List<GalaxyPair> = pairGalaxies()
        .map { galaxiesPair ->
            GalaxyPair(galaxiesPair, shortestPathLength(galaxiesPair, expansionDegree))
        }

    private fun pairGalaxies(): List<Pair<Galaxy, Galaxy>> {
        val galaxiesPairs = mutableListOf<Pair<Galaxy, Galaxy>>()
        for (i in galaxies.indices) {
            val galaxy1 = galaxies[i]
            for (j in (i + 1)..<galaxies.size) {
                val galaxy2 = galaxies[j]
                galaxiesPairs.add(Pair(galaxy1, galaxy2))
            }
        }
        return galaxiesPairs
    }

    private fun shortestPathLength(galaxies: Pair<Galaxy, Galaxy>, expansionDegree: Int): Long {
        val galaxy1 = galaxies.first
        val galaxy2 = galaxies.second
        // the shortest distance between two points (a, b) and (c, d)
        // is equals to mod(c-a) + mod(d-b)
        // here we also need to account for the expansion
        // each row or column without galaxy crossed counts one additional
        val rangeRow = if (galaxy1.row < galaxy2.row) {
            (galaxy1.row..galaxy2.row)
        } else {
            (galaxy2.row..galaxy1.row)
        }
        val rangeColumn = if (galaxy1.column < galaxy2.column) {
            (galaxy1.column..galaxy2.column)
        } else {
            (galaxy2.column..galaxy1.column)
        }

        val emptyRowsCrossed = rangeRow.toSet().intersect(rowsWithoutGalaxy).size
        val emptyColumnsCrossed = rangeColumn.toSet().intersect(columnsWithoutGalaxy).size.toLong()

        return (rangeRow.count() - 1) + (rangeColumn.count() - 1) +
                (emptyRowsCrossed * (expansionDegree - 1)) + (emptyColumnsCrossed * (expansionDegree - 1))
    }

    companion object {
        fun parse(input: List<String>): SpaceImage {
            val galaxies = mutableListOf<Galaxy>()
            input.forEachIndexed { i, lines ->
                lines.forEachIndexed { j, char ->
                    if (char == '#') {
                        galaxies.add(Galaxy(Coord(i, j)))
                    }
                }
            }
            val allRows = input.indices.toSet()
            val rowsWithGalaxy = galaxies.map(Galaxy::position).map(Coord::row).toSet()
            val rowsWithoutGalaxy = allRows - rowsWithGalaxy

            val allColumns = input[0].indices.toSet()
            val columnsWithGalaxy = galaxies.map(Galaxy::position).map(Coord::column).toSet()
            val columnsWithoutGalaxy = allColumns - columnsWithGalaxy

            return SpaceImage(galaxies, rowsWithoutGalaxy, columnsWithoutGalaxy)
        }
    }
}

private data class GalaxyPair(
    val galaxies: Pair<Galaxy, Galaxy>,
    val pathLength: Long
)

private data class Galaxy(
    val position: Coord
) {
    val row = position.row
    val column = position.column
}

private data class Coord(
    val row: Int,
    val column: Int
)