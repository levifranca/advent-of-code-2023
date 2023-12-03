package day03

import println
import readInput

fun main() {
    fun part1(input: List<String>): Int {
        val grid: Array<CharArray> = Array(input.size) { CharArray(input[0].length) }
        val numbersToken = mutableListOf<NumberToken>()
        input.forEachIndexed { i, line ->
            var tempNumber = ""
            var firstPos: Int? = null
            line.forEachIndexed { j, char ->
                grid[i][j] = char
                if (char.isDigit()) {
                    tempNumber += char
                    if (firstPos == null) {
                        firstPos = j
                    }
                } else {
                    if (tempNumber.isNotEmpty()) {
                        numbersToken.add(NumberToken(tempNumber.toInt(), i, firstPos!!, j - 1))
                    }
                    tempNumber = ""
                    firstPos = null
                }
            }
            if (tempNumber.isNotEmpty()) {
                numbersToken.add(NumberToken(tempNumber.toInt(), i, firstPos!!, grid[i].size - 1))
            }
        }

        val partNumbers = numbersToken.filter { it.isPartNumber(grid) }
        return partNumbers.sumOf { it.value }
    }

    fun part2(input: List<String>): Int {
        val gridColumnSize = input[0].length
        val numberTokensGrid = Array(input.size) { arrayOfNulls<NumberToken?>(gridColumnSize) }
        val asteriskTokens = mutableListOf<AsteriskToken>()
        input.forEachIndexed { i, lineString ->
            var tempNumber = ""
            var firstPos: Int? = null
            lineString.forEachIndexed { j, char ->
                if (char.isDigit()) {
                    tempNumber += char
                    if (firstPos == null) {
                        firstPos = j
                    }
                } else {
                    if (tempNumber.isNotEmpty()) {
                        val numToken = NumberToken(tempNumber.toInt(), i, firstPos!!, j - 1)

                        ((j - 1) downTo firstPos!!).forEach { column ->
                            numberTokensGrid[i][column] = numToken
                        }
                    }
                    tempNumber = ""
                    firstPos = null
                    if (char.isAsterisk()) {
                        asteriskTokens.add(AsteriskToken(i, j))
                    }
                }
            }
            if (tempNumber.isNotEmpty()) {
                val numToken = NumberToken(tempNumber.toInt(), i, firstPos!!, gridColumnSize - 1)
                ((gridColumnSize - 1) downTo firstPos!!).forEach { column ->
                    numberTokensGrid[i][column] = numToken
                }
            }
        }

        return asteriskTokens.sumOf { asteriskToken -> asteriskToken.ratio(numberTokensGrid) }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("day03/Day03_test")
    check(part1(testInput) == 4361)
    val testInput2 = readInput("day03/Day03_test")
    check(part2(testInput2) == 467835)

    val input = readInput("day03/Day03")
    part1(input).println()
    part2(input).println()
}


data class NumberToken(
    val value: Int,
    val lineNumber: Int,
    val firstPos: Int,
    val lastPos: Int
) {
    fun isPartNumber(grid: Array<CharArray>): Boolean {
        return hasSymbolLeft(grid)
                || hasSymbolTopLeft(grid)
                || hasSymbolOnTop(grid)
                || hasSymbolTopRight(grid)
                || hasSymbolRight(grid)
                || hasSymbolBottomRight(grid)
                || hasSymbolAtBottom(grid)
                || hasSymbolBottomLeft(grid)
    }

    private fun hasSymbolLeft(grid: Array<CharArray>): Boolean =
        firstPos != 0 && grid[lineNumber][firstPos - 1].isSymbol()

    private fun hasSymbolTopLeft(grid: Array<CharArray>): Boolean =
        firstPos != 0 && lineNumber != 0 && grid[lineNumber - 1][firstPos - 1].isSymbol()

    private fun hasSymbolOnTop(grid: Array<CharArray>): Boolean =
        lineNumber != 0 && (firstPos..lastPos).any { grid[lineNumber - 1][it].isSymbol() }

    private fun hasSymbolTopRight(grid: Array<CharArray>): Boolean =
        lineNumber != 0 && lastPos < grid[0].size - 1 && grid[lineNumber - 1][lastPos + 1].isSymbol()

    private fun hasSymbolRight(grid: Array<CharArray>): Boolean =
        lastPos < grid[0].size - 1 && grid[lineNumber][lastPos + 1].isSymbol()

    private fun hasSymbolBottomRight(grid: Array<CharArray>): Boolean =
        lineNumber < grid.size - 1 && lastPos < grid[0].size - 1 && grid[lineNumber + 1][lastPos + 1].isSymbol()

    private fun hasSymbolAtBottom(grid: Array<CharArray>): Boolean =
        lineNumber < grid.size - 1 && (lastPos downTo firstPos).any { grid[lineNumber + 1][it].isSymbol() }

    private fun hasSymbolBottomLeft(grid: Array<CharArray>): Boolean =
        lineNumber < grid.size - 1 && firstPos != 0 && grid[lineNumber + 1][firstPos - 1].isSymbol()

}

data class AsteriskToken(val posLine: Int, val posColumn: Int) {
    fun ratio(numberTokensGrid: Array<Array<NumberToken?>>): Int {
        val adjacentNumberTokens = getAdjacentNumberTokens(numberTokensGrid)
        if (adjacentNumberTokens.size < 2) {
            // not a gear
            return 0
        }

        return adjacentNumberTokens.fold(1) { acc, numberToken -> acc.times(numberToken.value) }
    }

    private fun getAdjacentNumberTokens(
        numberTokensGrid: Array<Array<NumberToken?>>
    ): Set<NumberToken> {
        val adjacentNumberTokens = mutableSetOf<NumberToken>()
        // top-left
        if (posLine != 0 && posColumn != 0) {
            val numToken = numberTokensGrid[posLine - 1][posColumn - 1]
            if (numToken != null) {
                adjacentNumberTokens.add(numToken)
            }
        }
        // top
        if (posLine != 0) {
            val numToken = numberTokensGrid[posLine - 1][posColumn]
            if (numToken != null) {
                adjacentNumberTokens.add(numToken)
            }
        }
        // top-right
        if (posLine != 0 && posColumn < numberTokensGrid[0].size - 1) {
            val numToken = numberTokensGrid[posLine - 1][posColumn + 1]
            if (numToken != null) {
                adjacentNumberTokens.add(numToken)
            }
        }
        // right
        if (posColumn < numberTokensGrid[0].size - 1) {
            val numToken = numberTokensGrid[posLine][posColumn + 1]
            if (numToken != null) {
                adjacentNumberTokens.add(numToken)
            }
        }
        // bottom-right
        if (posLine < numberTokensGrid.size - 1 && posColumn < numberTokensGrid[0].size - 1) {
            val numToken = numberTokensGrid[posLine + 1][posColumn + 1]
            if (numToken != null) {
                adjacentNumberTokens.add(numToken)
            }
        }
        // bottom
        if (posLine < numberTokensGrid.size - 1) {
            val numToken = numberTokensGrid[posLine + 1][posColumn]
            if (numToken != null) {
                adjacentNumberTokens.add(numToken)
            }
        }
        // bottom-left
        if (posLine < numberTokensGrid.size - 1 && posColumn != 0) {
            val numToken = numberTokensGrid[posLine + 1][posColumn - 1]
            if (numToken != null) {
                adjacentNumberTokens.add(numToken)
            }
        }
        // left
        if (posColumn != 0) {
            val numToken = numberTokensGrid[posLine][posColumn - 1]
            if (numToken != null) {
                adjacentNumberTokens.add(numToken)
            }
        }
        return adjacentNumberTokens
    }
}

private fun Char.isSymbol(): Boolean = !this.isDigit() && this != '.'

private fun Char.isAsterisk(): Boolean = this == '*'