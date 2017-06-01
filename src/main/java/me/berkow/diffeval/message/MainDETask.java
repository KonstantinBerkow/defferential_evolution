package me.berkow.diffeval.message;

import me.berkow.diffeval.problem.Population;
import me.berkow.diffeval.problem.Problem;

import java.io.Serializable;

/**
 * Created by konstantinberkow on 5/9/17.
 */
public class MainDETask implements Serializable {

    private final int maxIterationsCount;
    private final int maxStaleCount;
    private final Population population;
    private final float amplification;
    private final float crossoverProbability;
    private final int splitSize;
    private final Problem problem;
    private final double precision;

    public MainDETask(int maxIterationsCount, int maxStaleCount, Population population, float amplification,
                      float crossoverProbability, int splitSize, Problem problem, double precision) {
        this.maxIterationsCount = maxIterationsCount;
        this.maxStaleCount = maxStaleCount;
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

    public int getMaxStaleCount() {
        return maxStaleCount;
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

    public double getPrecision() {
        return precision;
    }

    @Override
    public String toString() {
        return "MainDETask{" +
                "maxIterationsCount=" + maxIterationsCount +
                ", maxStaleCount=" + maxStaleCount +
                ", population=" + population +
                ", amplification=" + amplification +
                ", crossoverProbability=" + crossoverProbability +
                ", splitSize=" + splitSize +
                ", problem=" + problem +
                ", precision=" + precision +
                '}';
    }
}
