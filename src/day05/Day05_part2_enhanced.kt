package day05

import println
import readInput
import java.time.Duration
import java.time.Instant

fun main() {
    fun part2(input: List<String>): Long {
        return Day5Part2Enhanced.Almanac.parse(input).lowestLocationNumberFromSeedRanges()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("day05/Day05_test")
    check(part2(testInput) == 46L)

    val input = readInput("day05/Day05")
    val startPart2 = Instant.now()
    part2(input).println()
    "Part 2 took ${Duration.between(startPart2, Instant.now())} ".println()
}

private fun LongRange.isTotallyBefore(other: LongRange): Boolean = this.last < other.first
private fun LongRange.isTotallyAfter(other: LongRange): Boolean = this.first > other.last

class Day5Part2Enhanced {
    class Almanac private constructor(
        private val seedsRanges: List<LongRange>,
        private val seedToSoilMap: NumberRangeMap,
        private val soilToFertilizerMap: NumberRangeMap,
        private val fertilizerToWaterMap: NumberRangeMap,
        private val waterToLightMap: NumberRangeMap,
        private val lightToTemperatureMap: NumberRangeMap,
        private val temperatureToHumidityMap: NumberRangeMap,
        private val humidityToLocationMap: NumberRangeMap
    ) {

        fun lowestLocationNumberFromSeedRanges(): Long = seedsRanges
            .asSequence()
            .flatMap(seedToSoilMap::transformRanges)
            .flatMap(soilToFertilizerMap::transformRanges)
            .flatMap(fertilizerToWaterMap::transformRanges)
            .flatMap(waterToLightMap::transformRanges)
            .flatMap(lightToTemperatureMap::transformRanges)
            .flatMap(temperatureToHumidityMap::transformRanges)
            .flatMap(humidityToLocationMap::transformRanges)
            .minOf { it.first }


        companion object {
            fun parse(input: List<String>): Almanac {
                val firstLineSplit = input[0].split(" ")
                val seeds = firstLineSplit.subList(1, firstLineSplit.size).map(String::toLong)
                val seedsRanges = (0..seeds.size - 2 step 2).map { i ->
                    val rangeStart = seeds[i]
                    val rangeEnd = (seeds[i] + seeds[i + 1])
                    (rangeStart until rangeEnd)
                }

                var inputLine = 2 // starts at line "seed-to-soil map:"
                val (seedToSoilMapInput, indexAfterSeedToSoil) = getNextMapInputAndIndex(input, inputLine)
                val seedToSoilMap = NumberRangeMap.parse(seedToSoilMapInput)

                inputLine = indexAfterSeedToSoil + 1 // skip blank line
                val (soilToFertilizerMapInput, indexAfterSoilToFertilizer) = getNextMapInputAndIndex(input, inputLine)
                val soilToFertilizerMap = NumberRangeMap.parse(soilToFertilizerMapInput)

                inputLine = indexAfterSoilToFertilizer + 1 // skip blank line
                val (fertilizerToWaterMapInput, indexAfterFertilizerToWater) = getNextMapInputAndIndex(input, inputLine)
                val fertilizerToWaterMap = NumberRangeMap.parse(fertilizerToWaterMapInput)

                inputLine = indexAfterFertilizerToWater + 1 // skip blank line
                val (waterToLightMapInput, indexAfterWaterToLight) = getNextMapInputAndIndex(input, inputLine)
                val waterToLightMap = NumberRangeMap.parse(waterToLightMapInput)

                inputLine = indexAfterWaterToLight + 1 // skip blank line
                val (lightToTemperatureMapInput, indexAfterLightToTemperature) = getNextMapInputAndIndex(
                    input,
                    inputLine
                )
                val lightToTemperatureMap = NumberRangeMap.parse(lightToTemperatureMapInput)

                inputLine = indexAfterLightToTemperature + 1 // skip blank line
                val (temperatureToHumidityMapInput, newIndex) = getNextMapInputAndIndex(input, inputLine)
                val temperatureToHumidityMap = NumberRangeMap.parse(temperatureToHumidityMapInput)

                inputLine = newIndex + 1 // skip blank line
                val (humidityToLocationMapInput, _) = getNextMapInputAndIndex(input, inputLine)
                val humidityToLocationMap = NumberRangeMap.parse(humidityToLocationMapInput)

                return Almanac(
                    seedsRanges,
                    seedToSoilMap,
                    soilToFertilizerMap,
                    fertilizerToWaterMap,
                    waterToLightMap,
                    lightToTemperatureMap,
                    temperatureToHumidityMap,
                    humidityToLocationMap
                )
            }

            private fun getNextMapInputAndIndex(input: List<String>, currIndex: Int): Pair<List<String>, Int> {
                val mapInput = mutableListOf<String>()
                var index = currIndex
                while (index < input.size && input[index].isNotEmpty()) {
                    mapInput.add(input[index])
                    index++
                }
                return Pair(mapInput, index)
            }
        }
    }

    class NumberRangeMap(
        private val rangeMappings: List<RangeMapping>
    ) {
        fun transformRanges(ogRange: LongRange): List<LongRange> {
            // *** NOT MY PROUDEST CODE ***
            val newRanges = mutableListOf<LongRange>()
            var currOgRange = ogRange
            val emptyRange = LongRange(-1, -1)
            rangeMappings
                .sortedBy { it.srcRangeStart }
                .forEach { rangeMapping ->
                    val srcRange = rangeMapping.srcRange
                    val srcDestOffset = rangeMapping.srcDestOffset

                    if (currOgRange.isTotallyBefore(srcRange) || currOgRange.isTotallyAfter(srcRange) || currOgRange == emptyRange) {
                        // skip any transformation
                        return@forEach
                    }
                    // there is an intersection
                    if (currOgRange.first < srcRange.first) { // ogRange starts before srcRange
                        // the non-intersecting part convert 1-1
                        newRanges.add(LongRange(currOgRange.first, srcRange.first - 1))

                        // the intersecting part convert using src-dest offset
                        if (currOgRange.last >= srcRange.last) {
                            // all of srcRange is contained in ogRange
                            newRanges.add(
                                LongRange(
                                    srcRange.first + srcDestOffset,
                                    srcRange.last + srcDestOffset
                                )
                            )
                            // move the ogRange to after srcRange.last
                            currOgRange = LongRange(srcRange.last + 1, currOgRange.last)
                        } else {
                            // ogRange finishes before srcRange
                            newRanges.add(
                                LongRange(
                                    srcRange.first + srcDestOffset,
                                    currOgRange.last + srcDestOffset
                                )
                            )
                            currOgRange = emptyRange
                        }
                    } else { // ogRange starts within srcRange

                        if (currOgRange.last <= srcRange.last) {
                            // ogRange is fully contained on srcRange
                            newRanges.add(
                                LongRange(
                                    currOgRange.first + srcDestOffset,
                                    currOgRange.last + srcDestOffset
                                )
                            )
                            currOgRange = emptyRange
                        } else {
                            // ogRange goes past srcRange
                            newRanges.add(
                                LongRange(
                                    currOgRange.first + srcDestOffset,
                                    srcRange.last + srcDestOffset
                                )
                            )
                            currOgRange = LongRange(srcRange.last+1, currOgRange.last)
                        }
                    }
                }
            if (currOgRange != emptyRange) {
                // add a 1-1 mapping for anything left
                newRanges.add(LongRange(currOgRange.first, currOgRange.last))
            }
            return newRanges
        }

        companion object {
            fun parse(input: List<String>) =
                NumberRangeMap(input.subList(1, input.size).map(RangeMapping::parse))
        }
    }

    data class RangeMapping(
        val destRangeStart: Long,
        val srcRangeStart: Long,
        val rangeLength: Long
    ) {
        val srcRange: LongRange = LongRange(srcRangeStart, srcRangeStart + rangeLength - 1)
        val srcDestOffset = destRangeStart - srcRangeStart
        companion object {
            fun parse(input: String): RangeMapping {
                val split = input.split(" ")
                return RangeMapping(split[0].toLong(), split[1].toLong(), split[2].toLong())
            }
        }
    }
}
