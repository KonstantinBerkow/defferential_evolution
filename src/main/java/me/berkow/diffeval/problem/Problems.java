package me.berkow.diffeval.problem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by konstantinberkow on 5/11/17.
 */
public final class Problems {

    public static Problem createProblemWithConstraints(int problemId, double[] lowerBounds, double[] upperBounds) {
        switch (problemId) {
            case 6:
                return new Problem(lowerBounds, upperBounds) {
                    @Override
                    public double calculate(double[] vector) {
                        double sum = 0;

                        for (int i = 0; i < vector.length; i++) {
                            double x = vector[i];
                            sum += (i + 1) * x * x * x * x;
                        }

                        return sum;
                    }
                };
            default:
                throw new IllegalArgumentException("Unknown getProblem id: " + problemId);
        }
    }

    public static double[] createRandomVector(Problem problem, Random random) {
        final int size = problem.getSize();
        final double[] result = new double[size];

        for (int i = 0; i < size; i++) {
            final double min = problem.getLowerConstraints()[i];
            final double max = problem.getUpperConstraints()[i];

            result[i] = min + (max - min) * random.nextDouble();
        }

        return result;
    }

    public static List<double[]> createRandomPopulation(int populationSize, Problem problem, Random random) {
        final List<double[]> population = new ArrayList<>(populationSize);

        for (int i = 0; i < populationSize; i++) {
            population.add(createRandomVector(problem, random));
        }

        return population;
    }
}
