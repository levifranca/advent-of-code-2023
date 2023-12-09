package day05

import println
import readInput
import java.time.Duration
import java.time.Instant

fun main() {
    fun part1(input: List<String>): Long {
        return Almanac.parse(input).lowestLocationNumber()
    }

    fun part2(input: List<String>): Long {
        return Almanac.parse(input).lowestLocationNumberFromSeedRanges()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("day05/Day05_test")
    check(part1(testInput) == 35L)
    check(part2(testInput) == 46L)

    val input = readInput("day05/Day05")
    val startPart1 = Instant.now()
    part1(input).println()
    "Part 1 took ${Duration.between(startPart1, Instant.now())} ".println()
    val startPart2 = Instant.now()
    part2(input).println()
    "Part 2 took ${Duration.between(startPart2, Instant.now())} ".println()
}

class Almanac private constructor(
    private val seeds: List<Long>,
    private val seedsRanges: List<LongRange>,
    private val seedToSoilMap: NumberRangeMap,
    private val soilToFertilizerMap: NumberRangeMap,
    private val fertilizerToWaterMap: NumberRangeMap,
    private val waterToLightMap: NumberRangeMap,
    private val lightToTemperatureMap: NumberRangeMap,
    private val temperatureToHumidityMap: NumberRangeMap,
    private val humidityToLocationMap: NumberRangeMap
) {
    fun lowestLocationNumber(): Long = seeds
        .asSequence()
        .map(seedToSoilMap::fromSourceToDestination)
        .map(soilToFertilizerMap::fromSourceToDestination)
        .map(fertilizerToWaterMap::fromSourceToDestination)
        .map(waterToLightMap::fromSourceToDestination)
        .map(lightToTemperatureMap::fromSourceToDestination)
        .map(temperatureToHumidityMap::fromSourceToDestination)
        .map(humidityToLocationMap::fromSourceToDestination)
        .min()

    // This takes time...
    fun lowestLocationNumberFromSeedRanges(): Long = seedsRanges
        .asSequence()
        .flatten()
        .map(seedToSoilMap::fromSourceToDestination)
        .map(soilToFertilizerMap::fromSourceToDestination)
        .map(fertilizerToWaterMap::fromSourceToDestination)
        .map(waterToLightMap::fromSourceToDestination)
        .map(lightToTemperatureMap::fromSourceToDestination)
        .map(temperatureToHumidityMap::fromSourceToDestination)
        .map(humidityToLocationMap::fromSourceToDestination)
        .min()


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
            val (lightToTemperatureMapInput, indexAfterLightToTemperature) = getNextMapInputAndIndex(input, inputLine)
            val lightToTemperatureMap = NumberRangeMap.parse(lightToTemperatureMapInput)

            inputLine = indexAfterLightToTemperature + 1 // skip blank line
            val (temperatureToHumidityMapInput, newIndex) = getNextMapInputAndIndex(input, inputLine)
            val temperatureToHumidityMap = NumberRangeMap.parse(temperatureToHumidityMapInput)

            inputLine = newIndex + 1 // skip blank line
            val (humidityToLocationMapInput, _) = getNextMapInputAndIndex(input, inputLine)
            val humidityToLocationMap = NumberRangeMap.parse(humidityToLocationMapInput)

            return Almanac(
                seeds,
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
    fun fromSourceToDestination(source: Long): Long {
        val mapping =
            rangeMappings.singleOrNull { it.sourceRangeStart <= source && source < (it.sourceRangeStart + it.rangeLength) }

        if (mapping != null) {
            val offset = source - mapping.sourceRangeStart
            return mapping.destinationRangeStart + offset
        }
        return source
    }

    companion object {
        fun parse(input: List<String>) = NumberRangeMap(input.subList(1, input.size).map(RangeMapping::parse))
    }
}

data class RangeMapping(
    val destinationRangeStart: Long,
    val sourceRangeStart: Long,
    val rangeLength: Long
) {
    companion object {
        fun parse(input: String): RangeMapping {
            val split = input.split(" ")
            return RangeMapping(split[0].toLong(), split[1].toLong(), split[2].toLong())
        }
    }
}