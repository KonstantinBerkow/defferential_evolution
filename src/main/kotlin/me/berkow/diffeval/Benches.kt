package me.berkow.diffeval

import me.berkow.diffeval.message.SubTask
import me.berkow.diffeval.problem.bestValue
import me.berkow.diffeval.problem.createRandomPopulation
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.text.NumberFormat
import java.util.*

/**
 * Created by konstantinberkow on 6/22/17.
 */
fun main(args: Array<String>) {
    val map = args.toMap()

    val amplification = map.getAsFloat("-amplification", 0.6F)
    val crossover = map.getAsFloat("-crossover", 0.9F)

    val populationSize = map.getAsInt("-populationSize", 100)
    val randomSeed = map.getAsLong("-randomSeed", 1612)
    val precision = map.getAsInt("-precision", 6)

    val benchName = map.getOrDefault("-name", "noname" + System.currentTimeMillis())

    val size = map.getAsInt("-size", 10)

    val maxIterations = map.getAsInt("-maxIterations", 10000)

    val format = NumberFormat.getInstance()
    format.maximumFractionDigits = precision

    val problem100LowerConstraints = FloatArray(size) { -100F }
    val problem100UpperConstraints = FloatArray(size) { 100F }

    val problem10LowerConstraints = FloatArray(size) { -10F }
    val problem10UpperConstraints = FloatArray(size) { 10F }

    val problem30LowerConstraints = FloatArray(size) { -30F }
    val problem30UpperConstraints = FloatArray(size) { 30F }

    val problem128LowerConstraints = FloatArray(size) { -1.28F }
    val problem128UpperConstraints = FloatArray(size) { 1.28F }

    val problem500LowerConstraints = FloatArray(size) { -500F }
    val problem500UpperConstraints = FloatArray(size) { 500F }

    val problem512LowerConstraints = FloatArray(size) { -5.12F }
    val problem512UpperConstraints = FloatArray(size) { 5.12F }

    val problem32LowerConstraints = FloatArray(size) { -32F }
    val problem32UpperConstraints = FloatArray(size) { 32F }

    val problem600LowerConstraints = FloatArray(size) { -600F }
    val problem600UpperConstraints = FloatArray(size) { 600F }

    val problem50LowerConstraints = FloatArray(size) { -50F }
    val problem50UpperConstraints = FloatArray(size) { 50F }

    val problem05LowerConstraints = FloatArray(size) { -0.5F }
    val problem05UpperConstraints = FloatArray(size) { 0.5F }

    val file = Paths.get(benchName + ".csv")
    if (Files.exists(file)) {
        Files.delete(file)
    }
    Files.createFile(file)

    listOf<Problem>(
            Problems.createProblemWithConstraints(1, problem100LowerConstraints, problem100UpperConstraints),
            Problems.createProblemWithConstraints(2, problem100LowerConstraints, problem100UpperConstraints),
            Problems.createProblemWithConstraints(3, problem10LowerConstraints, problem10UpperConstraints),
            Problems.createProblemWithConstraints(4, problem100LowerConstraints, problem100UpperConstraints),
            Problems.createProblemWithConstraints(5, problem30LowerConstraints, problem30UpperConstraints),
            Problems.createProblemWithConstraints(6, problem128LowerConstraints, problem128UpperConstraints),
            Problems.createProblemWithConstraints(7, problem500LowerConstraints, problem500UpperConstraints),
            Problems.createProblemWithConstraints(8, problem512LowerConstraints, problem512UpperConstraints),
            Problems.createProblemWithConstraints(9, problem32LowerConstraints, problem32UpperConstraints),
            Problems.createProblemWithConstraints(10, problem600LowerConstraints, problem600UpperConstraints),
            Problems.createProblemWithConstraints(11, problem50LowerConstraints, problem50UpperConstraints),
            Problems.createProblemWithConstraints(12, problem05LowerConstraints, problem05UpperConstraints)
    )
            .map { createTask(maxIterations, populationSize, randomSeed, amplification, crossover, it, precision) }
            .map { task -> doAndMeasure({ Algorithms.standardDE(task, Random(randomSeed)) }) }
            .forEach { (result, time) ->
                val bestValue = result.bestValue()
                val iterations = result.iterationsCount
                val seconds = time / 1000000000.0

                val formateValue = format.format(bestValue).replace(",", "")

                Files.write(file, "${result.problem.id} , $formateValue , $iterations , ${format.format(seconds)}\n".toByteArray(),
                        StandardOpenOption.APPEND)
            }
}

fun createTask(maxIterations: Int, populationSize: Int, randomSeed: Long, amplification: Float, crossover: Float,
               problem: Problem, precision: Int): SubTask {
    val population = problem.createRandomPopulation(populationSize, Random(randomSeed))

    return SubTask(maxIterations, population, amplification, crossover, problem, precision)
}
