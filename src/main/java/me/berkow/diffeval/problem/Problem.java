package me.berkow.diffeval.problem;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by konstantinberkow on 5/11/17.
 */
public abstract class Problem implements Serializable {

    private final double[] lowerConstraints;
    private final double[] upperConstraints;

    public Problem(double[] lowerConstraints, double[] upperConstraints) {
        this.lowerConstraints = lowerConstraints;
        this.upperConstraints = upperConstraints;
    }

    public double[] getLowerConstraints() {
        return lowerConstraints;
    }

    public double[] getUpperConstraints() {
        return upperConstraints;
    }

    public int getSize() {
        return lowerConstraints.length;
    }

    public abstract double calculate(Member vector);

    @Override
    public String toString() {
        return "Problem{" +
                "lowerConstraints=" + Arrays.toString(lowerConstraints) +
                ", upperConstraints=" + Arrays.toString(upperConstraints) +
                '}';
    }
}
