package day15

import println
import readInput

fun main() {
    fun part1(input: List<String>): Int {
        return InitSequence.parse(input).steps.sumOf { it.HASH }
    }

    fun part2(input: List<String>): Int {
        return InitSequence.parse(input).totalFocusingPower()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("day15/Day15_test")
    check(part1(testInput) == 1320)
    check(part2(testInput) == 145)

    val input = readInput("day15/Day15")
    part1(input).println()
    part2(input).println()

}

private class InitSequence(
    val steps: List<Step>,
    val boxes: Array<MutableList<Lens>> = Array(256) { mutableListOf() }
) {

    fun execute() {
        for (step in steps) {
            val stepHash = step.hashLabel()
            val label = step.label

            val boxLenses = boxes[stepHash]

            if (step.isRemoval) {
                for(i in boxLenses.indices) {
                    val lens = boxLenses[i]
                    if (lens.label == label) {
                        boxLenses.removeAt(i)
                        break
                    }
                }
            } else {
                val newLens = Lens(label, step.focalLength)
                var foundInTheBox = false

                for(i in boxLenses.indices) {
                    val lens = boxLenses[i]
                    if (lens.label == label) {
                        boxLenses.removeAt(i)
                        boxLenses.add(i, newLens)
                        foundInTheBox = true
                    }
                }
                if (!foundInTheBox) {
                    boxLenses.add(newLens)
                }
            }
        }
    }

    fun totalFocusingPower(): Int {
        execute()
        var totalFocusingPower = 0
        boxes.forEachIndexed { i, lenses ->
            lenses.forEachIndexed { j, lens ->
                totalFocusingPower += (i + 1) * (j + 1) * lens.focalLength
            }
        }
        return totalFocusingPower
    }

    companion object {
        fun parse(input: List<String>): InitSequence {
            return InitSequence(input[0].split(",").map { Step(it) })
        }
    }
}

private data class Lens(
    val label: String,
    val focalLength: Int
)

private data class Step(
    private val stepValue: String
) {
    val isRemoval: Boolean
    val label: String
    val focalLength: Int
    val HASH: Int

    init {
        val dashPos = stepValue.indexOf('-')
        if(dashPos == -1) {
            isRemoval = false
            val equalsPos = stepValue.indexOf('=')
            label = stepValue.substring(0, equalsPos)
            focalLength = stepValue.substring(equalsPos+1, stepValue.length).toInt()
            HASH = hash()
        } else {
            isRemoval = true
            label = stepValue.substring(0, dashPos)
            focalLength = -1
            HASH = hash()
        }
    }

    fun hash(): Int {
        var value = 0
        for (c in stepValue) {
            value += c.code
            value *= 17
            value %= 256
        }
        return value
    }

    fun hashLabel(): Int {
        var value = 0
        for (c in label) {
            value += c.code
            value *= 17
            value %= 256
        }
        return value
    }
}
