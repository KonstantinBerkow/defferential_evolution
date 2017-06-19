package me.berkow.diffeval;

import me.berkow.diffeval.message.DEResult;
import me.berkow.diffeval.message.DETask;
import me.berkow.diffeval.problem.Member;
import me.berkow.diffeval.problem.Population;
import me.berkow.diffeval.problem.Problem;
import me.berkow.diffeval.problem.Problems;
import me.berkow.diffeval.util.Util;

import java.util.*;

/**
 * Created by konstantinberkow on 6/19/17.
 */
public class Algorithms {

    public static DEResult standardDE(DETask task, Random random) {
        final int maxIterationsCount = task.getMaxIterationsCount();
        final float amplification = task.getAmplification();
        final float crossoverProbability = task.getCrossoverProbability();
        final Problem problem = task.getProblem();
        final float precision = task.getPrecision();

        Population population = task.getInitialPopulation();

        for (int i = 0; i < maxIterationsCount; i++) {
            population = createNewGeneration(population, task, random);

            if (Problems.checkConvergence(population, problem, precision)) {
                return new DEResult(population, amplification, crossoverProbability, problem, "converged population", i);
            }
        }

        return new DEResult(population, amplification, crossoverProbability, problem, "max_iterations", maxIterationsCount);
    }

    public static Population createNewGeneration(final Population previousGeneration, DETask task, Random random) {
        final int populationSize = task.getPopulationSize();
        final List<Member> newVectors = new ArrayList<>(populationSize);

        final Problem problem = task.getProblem();
        final int size = problem.getSize();

        final float crossoverProbability = task.getCrossoverProbability();
        final float amplification = task.getAmplification();

        final Member[] members = previousGeneration.getMembers();

        final float[] lowerConstraints = problem.getLowerConstraints();
        final float[] upperConstraints = problem.getUpperConstraints();

        for (int i = 0; i < populationSize; i++) {
            final Member oldVector = members[i];
            final int[] indexes = Util.selectIndexes(0, populationSize, i, 3, random);
            final int mandatoryIndex = random.nextInt(size);
            final float[] newVector = new float[size];

            for (int j = 0; j < size; j++) {
                if (mandatoryIndex == j || random.nextDouble() < crossoverProbability) {
                    newVector[j] = members[indexes[0]].get(j) + amplification * (members[indexes[1]].get(j) - members[indexes[2]].get(j));
                } else {
                    newVector[j] = oldVector.get(j);
                }

                final float min = lowerConstraints[j];
                final float max = upperConstraints[j];

                if (newVector[j] < min || max < newVector[j]) {
                    newVector[j] = Util.nextFloat(min, max, random);
                }
            }

            final Member member = new Member(newVector);

            if (problem.calculate(member) < problem.calculate(oldVector)) {
                newVectors.add(member);
            } else {
                newVectors.add(oldVector);
            }
        }

        return new Population(newVectors.toArray(new Member[0]));
    }

    public static void main(String[] args) {
        final Map<String, String> argsMap = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            argsMap.put(args[i], args[i + 1]);
        }

        final int maxIterations = Util.getIntOrDefault(argsMap, "-maxIterations", 100);
        final int problemId = Util.getIntOrDefault(argsMap, "-problemId", 5);
        final int populationSize = Util.getIntOrDefault(argsMap, "-populationSize", 100);
        final long randomSeed = Util.getLongOrDefault(argsMap, "-randomSeed", -1);

        float amplification = Util.getFloatOrDefault(argsMap, "-amplification", 0.9F);
        amplification = Math.max(0, Math.min(amplification, 2));

        float crossoverProbability = Util.getFloatOrDefault(argsMap, "-crossover", 0.5F);
        crossoverProbability = Math.max(0, Math.min(crossoverProbability, 1));

        final float[] lowerBounds = Util.getFloatArrayOrThrow(argsMap, "-lowerBounds", "Supply lower bounds!");
        final float[] upperBounds = Util.getFloatArrayOrThrow(argsMap, "-upperBounds", "Supply upper bounds!");

        final float precision = Util.getFloatOrDefault(argsMap, "-precision", 1e-6F);

        final Problem problem = Problems.createProblemWithConstraints(problemId, lowerBounds, upperBounds);

        final Random random = randomSeed == -1 ? new Random() : new Random(randomSeed);

        final Population population = Problems.createRandomPopulation(populationSize, problem, random);

        final DETask task = new DETask(maxIterations, population, amplification, crossoverProbability, problem, precision);

        final DEResult result = standardDE(task, random);
        System.out.printf("Result population: %s\n", result.getPopulation());
        System.out.printf("Result type: %s\n", result.getType());
        System.out.printf("Result iterations: %d\n", result.getIterationsCount());
    }
}
