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

private fun Array<Array<Tile>>.print() {
    this.forEach {tiles ->
        tiles.forEach {
            print(it.pipe)
        }
        kotlin.io.println()
    }
}

data class PipeNetwork(
    val pipesGrid: Array<Array<Tile>>,
    val startingTile: Tile
) {

    fun stepsToFarthestFromStart(): Int {
        // perform BFS from startingTile
        // once it finds a visited tile,
        // the depth is the number of steps farthest from start
        val visited = mutableSetOf(startingTile)
        // list with tile, depth and previous tile coord
        val tilesToVisit = mutableListOf(
            Triple(pipesGrid[startingTile.connection1.first][startingTile.connection1.second], 1, startingTile.coord),
            Triple(pipesGrid[startingTile.connection2.first][startingTile.connection2.second], 1, startingTile.coord)
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
                    pipesGrid[nextConnection.first][nextConnection.second],
                    depth + 1,
                    currTile.coord
                )
            )
        }
    }

    private fun cleanGrid(): Array<Array<Tile>> {
        val cleanGrid = Array(pipesGrid.size) { Array(pipesGrid[0].size) { INVALID_TILE } }


        var prevTile = startingTile
        var currTile = pipesGrid[startingTile.connection1.first][startingTile.connection1.second]
        cleanGrid[startingTile.coord.first][startingTile.coord.second] = startingTile
        do {
            cleanGrid[currTile.coord.first][currTile.coord.second] = currTile
            val nextCoord = currTile.nextConnection(prevTile.coord)
            prevTile = currTile
            currTile = pipesGrid[nextCoord.first][nextCoord.second]
        } while (currTile != startingTile)

        cleanGrid.forEachIndexed { i, tiles ->
            tiles.forEachIndexed { j, tile ->
                if (tile == INVALID_TILE) {
                    cleanGrid[i][j] = Tile(Pipe.GROUND, Pair(i, j))
                }
            }
        }

        return cleanGrid
    }

    fun enclosedTiles(): Int {

        val cleanGrid = cleanGrid()
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
        cleanGrid.forEachIndexed { i, tiles ->
            var j = 0
            while (j < tiles.size) {
                val tile = tiles[j]
                if (tile.pipe == Pipe.GROUND) {
                    if(isInside) {
                        enclosedTiles.add(tile)
                    }
                }

                if(tile.pipe == Pipe.VERTICAL) {
                    isInside = !isInside
                }

                if (tile.pipe == Pipe.NORTH_EAST_90DEG/*L*/) {
                    // Found L, walk until we find either a J or a 7
                    j++
                    while (tiles[j].pipe != Pipe.NORTH_WEST_90DEG && tiles[j].pipe != Pipe.SOUTH_WEST_90DEG) {
                        j++
                    }
                    val foundTile = tiles[j]
                    if (foundTile.pipe == Pipe.SOUTH_WEST_90DEG) {
                        // found a L(-)*7
                        isInside = !isInside
                    }
                    // otherwise it is a L(-)*J, then nothing to do
                }

                if (tile.pipe == Pipe.SOUTH_EAST_90DEG/*F*/) {
                    // Found F, walk until we find either a J or a 7
                    j++
                    while (tiles[j].pipe != Pipe.NORTH_WEST_90DEG && tiles[j].pipe != Pipe.SOUTH_WEST_90DEG) {
                        // Found a '-', keep going
                        j++
                    }
                    val foundTile = tiles[j]
                    if (foundTile.pipe == Pipe.NORTH_WEST_90DEG) {
                        // found a F(-)*J
                        isInside = !isInside
                    }
                    // otherwise it is a F(-)*7, then nothing to do
                }
                j++
            }
        }

        return enclosedTiles.size
    }

    companion object {

        private val INVALID_TILE = Tile(Pipe.GROUND, Pair(-1, -1))
        fun parse(input: List<String>): PipeNetwork {
            val pipesChart = Array(input.size) { Array(input[0].length) { INVALID_TILE } }
            var startingTile = INVALID_TILE
            input.forEachIndexed { i, line ->
                line.forEachIndexed { j, char ->
                    if (char == 'S') {
                        startingTile = resolveStartingTile(input, Pair(i, j))
                        pipesChart[i][j] = startingTile
                    } else {
                        pipesChart[i][j] = Tile(Pipe.parse(char), Pair(i, j))
                    }
                }
            }

            return PipeNetwork(pipesChart, startingTile)
        }

        private fun resolveStartingTile(input: List<String>, coord: Pair<Int, Int>): Tile {
            val inputDimensions = Pair(input.size, input[0].length)
            val northCoord = coord + NORTH
            val tileNorth = if (isInvalidCoord(northCoord, inputDimensions)) {
                INVALID_TILE
            } else {
                Tile(Pipe.parse(input[northCoord.first][northCoord.second]), northCoord)
            }

            val eastCoord = coord + EAST
            val tileEast = if (isInvalidCoord(eastCoord, inputDimensions)) {
                INVALID_TILE
            } else {
                Tile(Pipe.parse(input[eastCoord.first][eastCoord.second]), eastCoord)
            }

            val southCoord = coord + SOUTH
            val tileSouth = if (isInvalidCoord(southCoord, inputDimensions)) {
                INVALID_TILE
            } else {
                Tile(Pipe.parse(input[southCoord.first][southCoord.second]), southCoord)
            }

            val westCoord = coord + WEST
            val tileWest = if (isInvalidCoord(westCoord, inputDimensions)) {
                INVALID_TILE
            } else {
                Tile(Pipe.parse(input[westCoord.first][westCoord.second]), westCoord)
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

            return Tile(
                Pipe.fromConnectingPair(Pair(connectingToStart[0].coord, connectingToStart[1].coord), coord),
                coord
            )
        }

        private fun isInvalidCoord(coord: Pair<Int, Int>, gridDimension: Pair<Int, Int>) =
            coord.first < 0 || coord.first >= gridDimension.first || coord.second < 0 || coord.second >= gridDimension.second
    }
}

data class Tile(
    val pipe: Pipe,
    val coord: Pair<Int, Int>
) {
    val connection1: Pair<Int, Int> = pipe.connection1(coord)
    val connection2: Pair<Int, Int> = pipe.connection2(coord)
    fun connectsTo(coord: Pair<Int, Int>) = this.connection1 == coord || this.connection2 == coord

    fun nextConnection(coord: Pair<Int, Int>) = if (coord == connection1) {
        connection2
    } else if (coord == connection2) {
        connection1
    } else {
        throw IllegalArgumentException("Passed coord $coord is not one of the connection of this pipe $this")
    }
}

enum class Pipe(
    private val value: Char,
    private val connection1Modifier: Pair<Int, Int>,
    private val connection2Modifier: Pair<Int, Int>,
) {

    /*
     * Connection always go clockwise starting at north. NORTH, EAST, SOUTH, WEST
     */
    VERTICAL('|', NORTH, SOUTH),        //    | is a vertical pipe connecting north and south.
    HORIZONTAL('-', EAST, WEST),        //    - is a horizontal pipe connecting east and west.
    NORTH_EAST_90DEG('L', NORTH, EAST), //    L is a 90-degree bend connecting north and east.
    NORTH_WEST_90DEG('J', NORTH, WEST), //    J is a 90-degree bend connecting north and west.
    SOUTH_WEST_90DEG('7', SOUTH, WEST), //    7 is a 90-degree bend connecting south and west.
    SOUTH_EAST_90DEG('F', EAST, SOUTH), //    F is a 90-degree bend connecting south and east.
    GROUND('.', Pair(0, 0), Pair(0, 0));//    . is ground; there is no pipe in this tile.

    fun connection1(coord: Pair<Int, Int>): Pair<Int, Int> = coord + connection1Modifier
    fun connection2(coord: Pair<Int, Int>): Pair<Int, Int> = coord + connection2Modifier

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

        fun fromConnectingPair(connectingPair: Pair<Pair<Int, Int>, Pair<Int, Int>>, pivot: Pair<Int, Int>): Pipe {
            // assuming connectingPair is in order: NORTH, EAST, SOUTH, WEST
            val offset: Pair<Pair<Int, Int>, Pair<Int, Int>> =
                Pair(connectingPair.first - pivot, connectingPair.second - pivot)


            return when (offset) {
                Pair(NORTH, SOUTH) -> VERTICAL
                Pair(EAST, WEST) -> HORIZONTAL
                Pair(NORTH, EAST) -> NORTH_EAST_90DEG
                Pair(NORTH, WEST) -> NORTH_WEST_90DEG
                Pair(EAST, SOUTH) -> SOUTH_EAST_90DEG
                Pair(SOUTH, WEST) -> SOUTH_WEST_90DEG
                else -> throw IllegalStateException("Could not find Pipe from connectingPair=$connectingPair and pivot=$pivot")
            }
        }
    }
}

private val NORTH = Pair(-1, 0)
private val SOUTH = Pair(1, 0)
private val EAST = Pair(0, 1)
private val WEST = Pair(0, -1)

private operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>): Pair<Int, Int> =
    Pair(this.first + other.first, this.second + other.second)

private operator fun Pair<Int, Int>.minus(other: Pair<Int, Int>): Pair<Int, Int> =
    Pair(this.first - other.first, this.second - other.second)