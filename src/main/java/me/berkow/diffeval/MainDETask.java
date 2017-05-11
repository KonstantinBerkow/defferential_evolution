package me.berkow.diffeval;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by konstantinberkow on 5/9/17.
 */
public class MainDETask implements Serializable {

    private final int problemId;
    private final int problemSize;
    private final int maxGenerationsCount;
    private final double[] lowerConstraints;
    private final double[] upperConstraints;

    public MainDETask(int problemId, int maxGenerationsCount, double[] lowerConstraints, double[] upperConstraints) {
        this.problemId = problemId;
        this.problemSize = upperConstraints.length;
        this.maxGenerationsCount = maxGenerationsCount;
        this.lowerConstraints = lowerConstraints;
        this.upperConstraints = upperConstraints;

        if (upperConstraints.length != lowerConstraints.length) {
            throw new IllegalArgumentException("Constraints size must be equal!");
        }
    }

    public int getProblemId() {
        return problemId;
    }

    public int getProblemSize() {
        return problemSize;
    }

    public int getMaxGenerationsCount() {
        return maxGenerationsCount;
    }

    public double[] getLowerConstraints() {
        return lowerConstraints;
    }

    public double[] getUpperConstraints() {
        return upperConstraints;
    }

    @Override
    public String toString() {
        return "MainDETask{" +
                "problemId=" + problemId +
                ", problemSize=" + problemSize +
                ", maxGenerationsCount=" + maxGenerationsCount +
                ", lowerConstraints=" + Arrays.toString(lowerConstraints) +
                ", upperConstraints=" + Arrays.toString(upperConstraints) +
                '}';
    }
}
