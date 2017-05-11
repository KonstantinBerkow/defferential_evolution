package me.berkow.diffeval.message;

import me.berkow.diffeval.problem.Population;
import me.berkow.diffeval.problem.Problem;

import java.io.Serializable;
import java.util.List;

/**
 * Created by konstantinberkow on 5/9/17.
 */
public class MainDETask implements Serializable {

    private final int maxIterationsCount;
    private final Population initialPopulation;
    private final float initialAmplification;
    private final float initialConvergence;
    private final int splitSize;
    private final Problem problem;

    public MainDETask(int maxIterationsCount, Population initialPopulation, float initialAmplification,
                      float initialConvergence, int splitSize, Problem problem) {
        this.maxIterationsCount = maxIterationsCount;
        this.initialPopulation = initialPopulation;
        this.initialAmplification = initialAmplification;
        this.initialConvergence = initialConvergence;
        this.splitSize = splitSize;
        this.problem = problem;
    }

    public int getPopulationSize() {
        return initialPopulation.size();
    }

    public int getMaxIterationsCount() {
        return maxIterationsCount;
    }

    public Population getInitialPopulation() {
        return initialPopulation;
    }

    public float getInitialAmplification() {
        return initialAmplification;
    }

    public float getInitialConvergence() {
        return initialConvergence;
    }

    public int getSplitSize() {
        return splitSize;
    }

    public Problem getProblem() {
        return problem;
    }

    @Override
    public String toString() {
        return "MainDETask{" +
                "maxIterationsCount=" + maxIterationsCount +
                ", initialPopulation=" + initialPopulation +
                ", initialAmplification=" + initialAmplification +
                ", initialConvergence=" + initialConvergence +
                ", splitSize=" + splitSize +
                ", getProblem=" + problem +
                '}';
    }
}
