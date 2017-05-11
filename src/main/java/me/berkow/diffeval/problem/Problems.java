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

    public static double calculateProblem1(double[] vector) {
        double sum = 0;

        for (double x : vector) {
            sum += x * x;
        }

        return sum;
    }

    public static double calculateProblem2(double[] vector) {
        double sum = 0;
        double product = 1;

        for (double x : vector) {
            double absX = Math.abs(x);
            sum += absX;
            product *= absX;
        }

        return sum + product;
    }

    public static double calculateProblem3(double[] vector) {
        double outerSum = 0;

        for (int i = 0; i < vector.length; i++) {
            double innerSum = 0;
            for (int j = 0; j <= i; j++) {
                innerSum += vector[j];
            }
            outerSum += innerSum;
        }

        return outerSum;
    }

    public static double calculateProblem4(double[] vector) {
        double max = Math.abs(vector[0]);
        for (int i = 1; i < vector.length; i++) {
            final double x = Math.abs(vector[i]);
            max = x > max ? x : max;
        }
        return max;
    }

    public static double calculateProblem5(double[] vector) {
        double sum = 0;

        for (int i = 0; i < vector.length - 1; i++) {
            final double x = vector[i];
            sum += 100 * (vector[i + 1] - x * x) * (vector[i + 1] - x * x) + (x - 1) * (x - 1);
        }

        return sum;
    }

    public static double calculateProblem6(double[] vector) {
        double sum = 0;

        for (int i = 0; i < vector.length; i++) {
            double x = vector[i];
            sum += (i + 1) * x * x * x * x;
        }

        return sum;
    }

    public static double calculateProblem7(double[] vector) {
        double sum = 0;

        for (int i = 0; i < vector.length; i++) {
            double x = vector[i];
            sum += x * Math.sin(Math.sqrt(Math.abs(x)));
        }

        return sum;
    }

    public static double calculateProblem8(double[] vector) {
        double tmp1 = calculateProblem1(vector);
        double part1 = 20 * Math.exp(-0.2 * Math.sqrt(tmp1));

        double tmp2 = 0;
        for (double x : vector) {
            tmp2 += Math.cos(2 * Math.PI * x);
        }
        double part2 = Math.exp(tmp2 / vector.length);

        return -part1 - part2 + 20 + Math.E;
    }

    public static double calculateProblem9(double[] vector) {
        double sum = 0;

        for (int i = 0; i < vector.length; i++) {
            double x = vector[i];
            sum += (x * x - 10 * Math.cos(2 * Math.PI * x) + 10);
        }

        return sum;
    }

    public static double calculateProblem10(double[] vector) {
        double f1Result = calculateProblem1(vector);

        double product = 1;
        for (int i = 0; i < vector.length; i++) {
            product *= Math.cos(vector[i] / Math.sqrt(i));
        }

        return f1Result / 4000 - product + 1;
    }

    public static Problem createProblemWithConstraints(int problemId, double[] lowerBounds, double[] upperBounds) {
        switch (problemId) {
            case 1:
                return new Problem(lowerBounds, upperBounds) {
                    @Override
                    public double calculate(Member vector) {
                        return calculateProblem1(vector.toArray());
                    }
                };
            case 2:
                return new Problem(lowerBounds, upperBounds) {
                    @Override
                    public double calculate(Member vector) {
                        return calculateProblem2(vector.toArray());
                    }
                };
            case 3:
                return new Problem(lowerBounds, upperBounds) {
                    @Override
                    public double calculate(Member vector) {
                        return calculateProblem3(vector.toArray());
                    }
                };

            case 4:
                return new Problem(lowerBounds, upperBounds) {
                    @Override
                    public double calculate(Member vector) {
                        return calculateProblem4(vector.toArray());
                    }
                };
            case 5:
                return new Problem(lowerBounds, upperBounds) {
                    @Override
                    public double calculate(Member vector) {
                        return calculateProblem5(vector.toArray());
                    }
                };
            case 6:
                return new Problem(lowerBounds, upperBounds) {
                    @Override
                    public double calculate(Member vector) {
                        return calculateProblem6(vector.toArray());
                    }
                };
            case 7:
                return new Problem(lowerBounds, upperBounds) {
                    @Override
                    public double calculate(Member vector) {
                        return calculateProblem7(vector.toArray());
                    }
                };
            case 8:
                return new Problem(lowerBounds, upperBounds) {
                    @Override
                    public double calculate(Member vector) {
                        return calculateProblem8(vector.toArray());
                    }
                };
            case 9:
                return new Problem(lowerBounds, upperBounds) {
                    @Override
                    public double calculate(Member vector) {
                        return calculateProblem9(vector.toArray());
                    }
                };
            case 10:
                return new Problem(lowerBounds, upperBounds) {
                    @Override
                    public double calculate(Member vector) {
                        return calculateProblem10(vector.toArray());
                    }
                };
            default:
                throw new IllegalArgumentException("Unknown problem id: " + problemId);
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
