package me.berkow.diffeval.problem;

import me.berkow.diffeval.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by konstantinberkow on 5/11/17.
 */
@SuppressWarnings("SameParameterValue")
public final class Problems {

    public static Problem createProblemWithConstraints(int problemId, double[] lowerBounds, double[] upperBounds) {
        switch (problemId) {
            case 6:
                return new Problem(lowerBounds, upperBounds) {
                    @Override
                    public double calculate(Member vector) {
                        double sum = 0;

                        for (int i = 0; i < vector.size(); i++) {
                            double x = vector.get(i);
                            sum += (i + 1) * x * x * x * x;
                        }

                        return sum;
                    }
                };
            default:
                throw new IllegalArgumentException("Unknown getProblem id: " + problemId);
        }
    }

    public static Member createRandomVector(Problem problem, Random random) {
        final int size = problem.getSize();
        final double[] result = new double[size];

        final double[] lowerConstraints = problem.getLowerConstraints();
        final double[] upperConstraints = problem.getUpperConstraints();

        for (int i = 0; i < size; i++) {
            final double min = lowerConstraints[i];
            final double max = upperConstraints[i];

            result[i] = Util.nextDouble(min, max, random);
        }

        return new Member(result);
    }

    public static Population createRandomPopulation(int populationSize, Problem problem, Random random) {
        final List<Member> population = new ArrayList<>(populationSize);

        for (int i = 0; i < populationSize; i++) {
            population.add(createRandomVector(problem, random));
        }

        return new Population(population.toArray(new Member[0]));
    }

    public static double calculatePopulationValue(Problem problem, Population population) {
        double value = 0;

        final Member[] members = population.getMembers();

        for (Member vector : members) {
            value += problem.calculate(vector);
        }

        return value / population.size();
    }
}
