package me.berkow.diffeval.problem

import me.berkow.diffeval.Problem
import me.berkow.diffeval.message.MainResult
import me.berkow.diffeval.message.SubResult
import me.berkow.diffeval.nextFloat
import java.util.*

/**
 * Created by konstantinberkow on 6/22/17.
 */
fun Population.bestValue(problem: Problem): Float {
    return members().map { member -> problem.calculate(member) }.sorted().first()
}

fun Population.averageValue(problem: Problem): Float {
    return members()
            .map { member -> problem.calculate(member) }
            .sum() / size()
}

fun Problem.createRandomVector(random: Random): Member {
    val array = (0..size - 1)
            .map { index -> random.nextFloat(lowerConstraints[index], upperConstraints[index]) }
            .toFloatArray()

    return Member(array)
}

fun Problem.createRandomPopulation(populationSize: Int, random: Random): Population {
    val population = (0..populationSize - 1)
            .map { createRandomVector(random) }
            .toTypedArray()

    return Population(population)
}

fun SubResult.bestValue() = population.bestValue(problem)

fun MainResult.bestValue() = population.bestValue(problem)