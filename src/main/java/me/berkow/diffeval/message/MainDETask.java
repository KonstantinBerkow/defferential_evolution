package me.berkow.diffeval.message;

import me.berkow.diffeval.problem.Population;
import me.berkow.diffeval.problem.Problem;

import java.io.Serializable;

/**
 * Created by konstantinberkow on 5/9/17.
 */
public class MainDETask implements Serializable {

    private final int maxIterationsCount;
    private final Population population;
    private final float amplification;
    private final float crossoverProbability;
    private final int splitSize;
    private final Problem problem;
    private final float precision;

    public MainDETask(int maxIterationsCount, Population population, float amplification,
                      float crossoverProbability, int splitSize, Problem problem, float precision) {
        this.maxIterationsCount = maxIterationsCount;
        this.population = population;
        this.amplification = amplification;
        this.crossoverProbability = crossoverProbability;
        this.splitSize = splitSize;
        this.problem = problem;
        this.precision = precision;
    }

    public int getMaxIterationsCount() {
        return maxIterationsCount;
    }

    public Population getPopulation() {
        return population;
    }

    public float getAmplification() {
        return amplification;
    }

    public float getCrossoverProbability() {
        return crossoverProbability;
    }

    public int getSplitSize() {
        return splitSize;
    }

    public Problem getProblem() {
        return problem;
    }

    public float getPrecision() {
        return precision;
    }

    @Override
    public String toString() {
        return "MainDETask{" +
                "maxIterationsCount=" + maxIterationsCount +
                ", population=" + population +
                ", amplification=" + amplification +
                ", crossoverProbability=" + crossoverProbability +
                ", splitSize=" + splitSize +
                ", problem=" + problem +
                ", precision=" + precision +
                '}';
    }
}
