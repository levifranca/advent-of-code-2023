package day10

import println
import readInput

fun main() {
    fun part1(input: List<String>): Int {
        return PipeNetwork.parse(input).stepsToFarthestFromStart()
    }

    fun part2(input: List<String>): Int {
        return PipeNetwork.parse(input).enclosedTiles()
    }

    // test if implementation meets criteria from the description, like:
    // part1
    val testInput = readInput("day10/Day10_test")
    check(part1(testInput) == 4)
    val testInput2 = readInput("day10/Day10_test2")
    check(part1(testInput2) == 8)
    // part2
    val testInput3 = readInput("day10/Day10_test3")
    check(part2(testInput3) == 4)
    val testInput4 = readInput("day10/Day10_test4")
    check(part2(testInput4) == 4)
    val testInput5 = readInput("day10/Day10_test5")
    check(part2(testInput5) == 8)
    val testInput6 = readInput("day10/Day10_test6")
    check(part2(testInput6) == 10)

    val input = readInput("day10/Day10")
    part1(input).println()
    part2(input).println()
}

private data class PipeNetwork(
    private val pipesGrid: PipeGrid,
    private val startingTile: Tile
) {

    fun stepsToFarthestFromStart(): Int {
        // perform BFS from startingTile
        // once it finds a visited tile,
        // the depth is the number of steps farthest from start
        val visited = mutableSetOf(startingTile)
        // list with tile, depth and previous tile coord
        val tilesToVisit = mutableListOf(
            Triple(pipesGrid[startingTile.connection1], 1, startingTile.coord),
            Triple(pipesGrid[startingTile.connection2], 1, startingTile.coord)
        )
        while (true) {
            if (tilesToVisit.isEmpty()) {
                throw IllegalStateException("Run out of tiles to visit")
            }
            val (currTile, depth, previousTileCoord) = tilesToVisit.removeAt(0)
            if (visited.contains(currTile)) {
                return depth
            }
            visited.add(currTile)
            val nextConnection = currTile.nextConnection(previousTileCoord)
            tilesToVisit.add(
                Triple(
                    pipesGrid[nextConnection],
                    depth + 1,
                    currTile.coord
                )
            )
        }
    }

    fun enclosedTiles() = pipesGrid.enclosedTiles(startingTile)

    companion object {

        val INVALID_TILE = Tile(Pipe.GROUND, Coord(-1, -1))
        fun parse(input: List<String>): PipeNetwork {
            val pipesChart = PipeGrid(input.size, input[0].length)
            var startingTile = INVALID_TILE
            input.forEachIndexed { i, line ->
                line.forEachIndexed { j, char ->
                    val tileCoord = Coord(i, j)
                    val tile = if (char == 'S') {
                        startingTile = resolveStartingTile(input, tileCoord)
                        startingTile
                    } else {
                        Tile(Pipe.parse(char), tileCoord)
                    }
                    pipesChart[tileCoord] = tile
                }
            }

            return PipeNetwork(pipesChart, startingTile)
        }

        private fun resolveStartingTile(input: List<String>, coord: Coord): Tile {
            val northCoord = coord.north()
            val tileNorth = if (northCoord.isInvalidCoord(input.size, input[0].length)) {
                INVALID_TILE
            } else {
                Tile(Pipe.parse(input.at(northCoord)), northCoord)
            }

            val eastCoord = coord.east()
            val tileEast = if (eastCoord.isInvalidCoord(input.size, input[0].length)) {
                INVALID_TILE
            } else {
                Tile(Pipe.parse(input.at(eastCoord)), eastCoord)
            }

            val southCoord = coord.south()
            val tileSouth = if (southCoord.isInvalidCoord(input.size, input[0].length)) {
                INVALID_TILE
            } else {
                Tile(Pipe.parse(input.at(southCoord)), southCoord)
            }

            val westCoord = coord.west()
            val tileWest = if (westCoord.isInvalidCoord(input.size, input[0].length)) {
                INVALID_TILE
            } else {
                Tile(Pipe.parse(input.at(westCoord)), westCoord)
            }

            val connectingToStart = listOf(tileNorth, tileEast, tileSouth, tileWest)
                .filter { it != INVALID_TILE }
                .filter { it.connectsTo(coord) }
            if (connectingToStart.size != 2) {
                throw IllegalStateException(
                    """
                    Could not find exactly 2 tiles connecting to starting tile:
                     connectingToStart: $connectingToStart
                     startCoord: $coord
                     tileNorth: $tileNorth
                     tileEast: $tileEast
                     tileSouth: $tileSouth
                     tileWest: $tileWest
                    """.trimIndent()
                )
            }

            val pipe = when (Pair(connectingToStart[0].coord, connectingToStart[1].coord)) {
                Pair(northCoord, southCoord) -> Pipe.VERTICAL
                Pair(eastCoord, westCoord) -> Pipe.HORIZONTAL
                Pair(northCoord, eastCoord) -> Pipe.NORTH_EAST_90DEG
                Pair(northCoord, westCoord) -> Pipe.NORTH_WEST_90DEG
                Pair(eastCoord, southCoord) -> Pipe.SOUTH_EAST_90DEG
                Pair(southCoord, westCoord) -> Pipe.SOUTH_WEST_90DEG
                else -> throw IllegalStateException("Could not find Pipe from $connectingToStart")
            }
            return Tile(pipe, coord)
        }

    }
}

private class PipeGrid(
    private val matrix: Array<Array<Tile>>
) {

    private val lineSize = matrix.size
    private val columnSize = matrix[0].size

    constructor(lineSize: Int, columnSize: Int) :
            this(Array(lineSize) { Array(columnSize) { PipeNetwork.INVALID_TILE } })

    fun print() {
        matrix.forEach { tiles ->
            tiles.forEach {
                print(it.pipe)
            }
            kotlin.io.println()
        }
    }

    operator fun set(coord: Coord, value: Tile) {
        matrix[coord.line][coord.column] = value
    }

    operator fun get(coord: Coord): Tile = matrix[coord.line][coord.column]


    private fun cleanGrid(startingTile: Tile): PipeGrid {
        val cleanGrid = PipeGrid(lineSize = lineSize, columnSize = columnSize)

        var prevTile = startingTile
        var currTile = this[startingTile.connection1]
        cleanGrid[startingTile.coord] = startingTile
        do {
            cleanGrid[currTile.coord] = currTile
            val nextCoord = currTile.nextConnection(prevTile.coord)
            prevTile = currTile
            currTile = this[nextCoord]
        } while (currTile != startingTile)

        cleanGrid.convertInvalidToGroundTiles()

        return cleanGrid
    }

    fun enclosedTiles(startingTile: Tile): Int {

        val cleanGrid = cleanGrid(startingTile)
        val enclosedTiles = mutableSetOf<Tile>()
        var isInside = false

        /*
            We go line by line, left-to-right in a clean grind (containing only pipes in the loop)
            We have a boolean signalling if we are inside or outside the loop
            Whenever we read:
              - a | pipe; or
              - a L7 pipe sequence (might have any number of - in between, f.e L---7); or
              - an FJ pipe sequence (might have any number of - in between, f.e F---J); or
             It means we crossed the loop then we flip the boolean.
             Whenever we read a . (ground), if we are inside the loop, it is an enclosed tile.
         */
        cleanGrid.forEachLine { tiles ->
            var j = 0
            while (j < tiles.size) {
                val tile = tiles[j]
                if (tile.pipe == Pipe.GROUND) {
                    if (isInside) {
                        enclosedTiles.add(tile)
                    }
                }

                if (tile.pipe == Pipe.VERTICAL) {
                    isInside = !isInside
                }

                if (tile.pipe == Pipe.NORTH_EAST_90DEG/*L*/ || tile.pipe == Pipe.SOUTH_EAST_90DEG/*F*/) {
                    val initCurvePipe = tile.pipe
                    // Found L, walk until we find either a J or a 7
                    j++
                    while (tiles[j].pipe == Pipe.HORIZONTAL) { j++ }
                    val finalCurvePipe = tiles[j].pipe
                    when (Pair(initCurvePipe, finalCurvePipe)) {
                        Pair(Pipe.NORTH_EAST_90DEG, Pipe.SOUTH_WEST_90DEG), // L7
                        Pair(Pipe.SOUTH_EAST_90DEG, Pipe.NORTH_WEST_90DEG) -> { // FJ
                            isInside = !isInside
                        }

                        else -> {} // do nothing
                    }
                }
                j++
            }
        }

        return enclosedTiles.size
    }

    private fun forEachLine(action: (Array<Tile>) -> Unit) {
        for (tiles in matrix) action(tiles)
    }

    private fun convertInvalidToGroundTiles() {
        matrix.forEachIndexed { i, tiles ->
            tiles.forEachIndexed { j, tile ->
                if (tile == PipeNetwork.INVALID_TILE) {
                    matrix[i][j] = Tile(Pipe.GROUND, Coord(i, j))
                }
            }
        }
    }
}

private fun List<String>.at(coord: Coord): Char = this[coord.line][coord.column]

private data class Coord(val line: Int, val column: Int) {
    fun isInvalidCoord(lineSize: Int, columnSize: Int) =
        line < 0 || line >= lineSize || column < 0 || column >= columnSize

    fun north() = NORTH.invoke(this)
    fun east() = EAST.invoke(this)
    fun south() = SOUTH.invoke(this)
    fun west() = WEST.invoke(this)

    companion object {
        val NORTH: (Coord) -> Coord = { coord -> Coord(coord.line - 1, coord.column) }
        val EAST: (Coord) -> Coord = { coord -> Coord(coord.line, coord.column + 1) }
        val SOUTH: (Coord) -> Coord = { coord -> Coord(coord.line + 1, coord.column) }
        val WEST: (Coord) -> Coord = { coord -> Coord(coord.line, coord.column - 1) }
    }
}

private data class Tile(
    val pipe: Pipe,
    val coord: Coord
) {
    val connection1: Coord = pipe.connection1(coord)
    val connection2: Coord = pipe.connection2(coord)
    fun connectsTo(coord: Coord) = this.connection1 == coord || this.connection2 == coord

    fun nextConnection(coord: Coord) = when (coord) {
        connection1 -> connection2
        connection2 -> connection1
        else -> throw IllegalArgumentException("Passed coord $coord is not one of the connection of this pipe $this")
    }
}

private enum class Pipe(
    private val value: Char,
    private val connection1Modifier: (Coord) -> Coord,
    private val connection2Modifier: (Coord) -> Coord,
) {

    /*
     * Connection always go clockwise starting at north. NORTH, EAST, SOUTH, WEST
     */
    VERTICAL('|', Coord.NORTH, Coord.SOUTH),        //    | is a vertical pipe connecting north and south.
    HORIZONTAL('-', Coord.EAST, Coord.WEST),        //    - is a horizontal pipe connecting east and west.
    NORTH_EAST_90DEG('L', Coord.NORTH, Coord.EAST), //    L is a 90-degree bend connecting north and east.
    NORTH_WEST_90DEG('J', Coord.NORTH, Coord.WEST), //    J is a 90-degree bend connecting north and west.
    SOUTH_WEST_90DEG('7', Coord.SOUTH, Coord.WEST), //    7 is a 90-degree bend connecting south and west.
    SOUTH_EAST_90DEG('F', Coord.EAST, Coord.SOUTH), //    F is a 90-degree bend connecting south and east.
    GROUND('.', { it }, { it });//    . is ground; there is no pipe in this tile.

    fun connection1(coord: Coord): Coord = connection1Modifier.invoke(coord)
    fun connection2(coord: Coord): Coord = connection2Modifier.invoke(coord)

    override fun toString(): String {
        return value.toString()
    }

    companion object {
        fun parse(input: Char): Pipe = when (input) {
            '|' -> VERTICAL
            '-' -> HORIZONTAL
            'L' -> NORTH_EAST_90DEG
            'J' -> NORTH_WEST_90DEG
            '7' -> SOUTH_WEST_90DEG
            'F' -> SOUTH_EAST_90DEG
            '.' -> GROUND
            else -> throw IllegalArgumentException("Unexpected char $input when parsing Pipe")
        }
    }
}