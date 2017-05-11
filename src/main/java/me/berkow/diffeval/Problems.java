package me.berkow.diffeval;

/**
 * Created by konstantinberkow on 5/11/17.
 */
public class Problems {

    public static double problem6(double[] vector) {
        double sum = 0;

        for (int i = 0; i < vector.length; i++) {
            double x = vector[i];
            sum += (i + 1) * x * x * x * x;
        }

        return sum;
    }

    public static double problem7(double[] vector) {
        double sum = 0;

        for (double x : vector) {
            sum += x * Math.sin(Math.sqrt(Math.abs(x)));
        }

        return sum;
    }
}
