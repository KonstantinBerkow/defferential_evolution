package me.berkow.diffeval.message;

import me.berkow.diffeval.problem.Problem;

import java.io.Serializable;
import java.util.List;

/**
 * Created by konstantinberkow on 5/10/17.
 */
public class DETask implements Serializable {

    private final int maxIterationsCount;
    private final List<double[]> initialPopulation;
    private final float amplification;
    private final float convergence;
    private final Problem problem;

    public DETask(int maxIterationsCount, List<double[]> initialPopulation, float amplification, float convergence, Problem problem) {
        this.maxIterationsCount = maxIterationsCount;
        this.initialPopulation = initialPopulation;
        this.amplification = amplification;
        this.convergence = convergence;
        this.problem = problem;
    }

    public int getMaxIterationsCount() {
        return maxIterationsCount;
    }

    public List<double[]> getInitialPopulation() {
        return initialPopulation;
    }

    public float getAmplification() {
        return amplification;
    }

    public float getConvergence() {
        return convergence;
    }

    public Problem getProblem() {
        return problem;
    }

    @Override
    public String toString() {
        return "DETask{" +
                "maxIterationsCount=" + maxIterationsCount +
                ", initialPopulation=" + initialPopulation +
                ", amplification=" + amplification +
                ", convergence=" + convergence +
                ", problem=" + problem +
                '}';
    }
}
