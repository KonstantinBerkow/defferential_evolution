package me.berkow.diffeval.message;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by konstantinberkow on 5/9/17.
 */
public class MainResult implements Serializable {

    private final int problemId;
    private final double[] result;

    public MainResult(int problemId, double[] result) {
        this.problemId = problemId;
        this.result = result;
    }

    public int getProblemId() {
        return problemId;
    }

    public double[] getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "MainResult{" +
                "problemId=" + problemId +
                ", result=" + Arrays.toString(result) +
                '}';
    }
}
