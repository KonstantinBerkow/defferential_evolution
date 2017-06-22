package me.berkow.diffeval;

import me.berkow.diffeval.problem.Member;
import me.berkow.diffeval.problem.Problem;

/**
 * Created by konstantinberkow on 5/11/17.
 */
@SuppressWarnings("SameParameterValue")
public final class Problems {
    private static final int K_MAX = 26;
    private static final float[] AK = new float[K_MAX];
    private static final float[] BK = new float[K_MAX];

    static {
        AK[0] = 1;
        BK[0] = (float) Math.PI;
        for (int i = 1; i < K_MAX; i++) {
            AK[i] = AK[i - 1] * .5F;
            BK[i] = BK[i - 1] * 3;
        }
    }

    //Sphere
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

    //looks like problem in definition, {0...0} won't be it's minimum maybe abs needed
    public static float calculateProblem3(float[] vector) {
        float outerSum = 0;

        for (int i = 0; i < vector.length; i++) {
            float innerSum = 0;
            for (int j = 0; j <= i; j++) {
                innerSum += Math.abs(vector[j]);
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

    // Rosenbrock
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

    //looks like min is {-418.9829, ..., -418.9828} not {418.9828, ..., 418.9828}
    public static float calculateProblem7(float[] vector) {
        float sum = 0;

        for (int i = 0; i < vector.length; i++) {
            float x = vector[i];
            sum += x * Math.sin(Math.sqrt(Math.abs(x)));
        }

        return sum;
    }

    //Rastrigin
    public static float calculateProblem8(float[] vector) {
        float sum = 0;

        for (int i = 0; i < vector.length; i++) {
            float x = vector[i];
            sum += (x * x - 10 * Math.cos(2 * Math.PI * x) + 10);
        }

        return sum;
    }

    //Ackley’s
    public static float calculateProblem9(float[] vector) {
        float tmp1 = calculateProblem1(vector);
        float part1 = (float) (20 * Math.exp(-0.2 * Math.sqrt(tmp1)));

        float tmp2 = 0;
        for (float x : vector) {
            tmp2 += Math.cos(2 * Math.PI * x);
        }
        float part2 = (float) Math.exp(tmp2 / vector.length);

        return (float) (-part1 - part2 + 20 + Math.E);
    }

    //Griewangk
    public static float calculateProblem10(float[] vector) {
        float f1Result = calculateProblem1(vector);

        float product = 1;
        for (int i = 0; i < vector.length; i++) {
            product *= Math.cos(vector[i] / Math.sqrt(i + 1));
        }

        return f1Result / 4000 - product + 1;
    }

    //Goldstein3 with penalty
    public static float calculateProblem11(float[] vector) {
        float penalty = 0;

        for (int i = 0; i < vector.length; i++) {
            penalty += penalty(vector[i], 10, 100, 4);
        }

        return goldstein3(vector) + penalty;
    }

    //Goldstein3
    public static float goldstein3(float[] array) {
        final int size = array.length;

        float result = 0;

        {
            final double tmp = Math.sin(Math.PI * array[0]);
            result += 10 * tmp * tmp;
        }

        for (int i = 0; i < size - 1; i++) {
            final float tmp1 = array[i] - 1;
            final double tmp2 = Math.sin(Math.PI * array[i + 1]);

            result += tmp1 * tmp1 * (1 + 10 * tmp2 * tmp2);
        }

        {
            final double tmp = array[size - 1] - 1;
            result += tmp * tmp;
        }

        return (float) (Math.PI * result / size);
    }

    public static float penalty(float z, float a, float k, float m) {
        if (z > a) {
            return (float) (k * Math.pow(z - a, m));
        } else if (z < -a) {
            return (float) (k * Math.pow(-z - a, m));
        } else {
            return 0;
        }
    }

    public static float calculateProblem12(float[] vector) {
        float result = 0;
        final int n = vector.length;

        for (int i = 0; i < n; i++) {
            for (int k = 0; k < 26; k++) {
                result += AK[k] * Math.cos(2 * BK[k] * (vector[i] + .5F));
            }
        }

        float minus = 0;
        for (int k = 0; k < 26; k++) {
            minus += AK[k] * Math.cos(BK[k]);
        }

        return result - n * minus;
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
            case 11:
                return new Problem(lowerBounds, upperBounds) {
                    @Override
                    public float calculate(Member vector) {
                        return calculateProblem11(vector.toArray());
                    }
                };
            case 12:
                return new Problem(lowerBounds, upperBounds) {
                    @Override
                    public float calculate(Member vector) {
                        return calculateProblem12(vector.toArray());
                    }
                };
            default:
                throw new IllegalArgumentException("Unknown problem id: " + problemId);
        }
    }
}
