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

    public static float calculateProblem1(float[] vector) {
        float sum = 0;

        for (float x : vector) {
            sum += x * x;
        }

        return sum;
    }

    public static float calculateProblem2(float[] vector) {
        float sum = 0;
        float product = 1;

        for (float x : vector) {
            float absX = Math.abs(x);
            sum += absX;
            product *= absX;
        }

        return sum + product;
    }

    public static float calculateProblem3(float[] vector) {
        float outerSum = 0;

        for (int i = 0; i < vector.length; i++) {
            float innerSum = 0;
            for (int j = 0; j <= i; j++) {
                innerSum += vector[j];
            }
            outerSum += innerSum;
        }

        return outerSum;
    }

    public static float calculateProblem4(float[] vector) {
        float max = Math.abs(vector[0]);
        for (int i = 1; i < vector.length; i++) {
            final float x = Math.abs(vector[i]);
            max = x > max ? x : max;
        }
        return max;
    }

    public static float calculateProblem5(float[] vector) {
        float sum = 0;

        for (int i = 0; i < vector.length - 1; i++) {
            final float x = vector[i];
            sum += 100 * (vector[i + 1] - x * x) * (vector[i + 1] - x * x) + (x - 1) * (x - 1);
        }

        return sum;
    }

    public static float calculateProblem6(float[] vector) {
        float sum = 0;

        for (int i = 0; i < vector.length; i++) {
            float x = vector[i];
            sum += (i + 1) * x * x * x * x;
        }

        return sum;
    }

    public static float calculateProblem7(float[] vector) {
        float sum = 0;

        for (int i = 0; i < vector.length; i++) {
            float x = vector[i];
            sum += x * Math.sin(Math.sqrt(Math.abs(x)));
        }

        return sum;
    }

    public static float calculateProblem8(float[] vector) {
        float tmp1 = calculateProblem1(vector);
        float part1 = (float) (20 * Math.exp(-0.2 * Math.sqrt(tmp1)));

        float tmp2 = 0;
        for (float x : vector) {
            tmp2 += Math.cos(2 * Math.PI * x);
        }
        float part2 = (float) Math.exp(tmp2 / vector.length);

        return (float) (-part1 - part2 + 20 + Math.E);
    }

    public static float calculateProblem9(float[] vector) {
        float sum = 0;

        for (int i = 0; i < vector.length; i++) {
            float x = vector[i];
            sum += (x * x - 10 * Math.cos(2 * Math.PI * x) + 10);
        }

        return sum;
    }

    public static float calculateProblem10(float[] vector) {
        float f1Result = calculateProblem1(vector);

        float product = 1;
        for (int i = 0; i < vector.length; i++) {
            product *= Math.cos(vector[i] / Math.sqrt(i));
        }

        return f1Result / 4000 - product + 1;
    }

    public static Problem createProblemWithConstraints(int problemId, float[] lowerBounds, float[] upperBounds) {
        switch (problemId) {
            case 1:
                return new Problem(lowerBounds, upperBounds) {
                    @Override
                    public float calculate(Member vector) {
                        return calculateProblem1(vector.toArray());
                    }
                };
            case 2:
                return new Problem(lowerBounds, upperBounds) {
                    @Override
                    public float calculate(Member vector) {
                        return calculateProblem2(vector.toArray());
                    }
                };
            case 3:
                return new Problem(lowerBounds, upperBounds) {
                    @Override
                    public float calculate(Member vector) {
                        return calculateProblem3(vector.toArray());
                    }
                };

            case 4:
                return new Problem(lowerBounds, upperBounds) {
                    @Override
                    public float calculate(Member vector) {
                        return calculateProblem4(vector.toArray());
                    }
                };
            case 5:
                return new Problem(lowerBounds, upperBounds) {
                    @Override
                    public float calculate(Member vector) {
                        return calculateProblem5(vector.toArray());
                    }
                };
            case 6:
                return new Problem(lowerBounds, upperBounds) {
                    @Override
                    public float calculate(Member vector) {
                        return calculateProblem6(vector.toArray());
                    }
                };
            case 7:
                return new Problem(lowerBounds, upperBounds) {
                    @Override
                    public float calculate(Member vector) {
                        return calculateProblem7(vector.toArray());
                    }
                };
            case 8:
                return new Problem(lowerBounds, upperBounds) {
                    @Override
                    public float calculate(Member vector) {
                        return calculateProblem8(vector.toArray());
                    }
                };
            case 9:
                return new Problem(lowerBounds, upperBounds) {
                    @Override
                    public float calculate(Member vector) {
                        return calculateProblem9(vector.toArray());
                    }
                };
            case 10:
                return new Problem(lowerBounds, upperBounds) {
                    @Override
                    public float calculate(Member vector) {
                        return calculateProblem10(vector.toArray());
                    }
                };
            default:
                throw new IllegalArgumentException("Unknown problem id: " + problemId);
        }
    }

    public static Member createRandomVector(Problem problem, Random random) {
        final int size = problem.getSize();
        final float[] result = new float[size];

        final float[] lowerConstraints = problem.getLowerConstraints();
        final float[] upperConstraints = problem.getUpperConstraints();

        for (int i = 0; i < size; i++) {
            final float min = lowerConstraints[i];
            final float max = upperConstraints[i];

            result[i] = Util.nextFloat(min, max, random);
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

    public static float calculatePopulationValue(Problem problem, Population population) {
        float value = 0;

        final Member[] members = population.getMembers();

        for (Member vector : members) {
            value += problem.calculate(vector);
        }

        return value / population.size();
    }
}
