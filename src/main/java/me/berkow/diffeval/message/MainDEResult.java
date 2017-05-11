package me.berkow.diffeval.message;

import me.berkow.diffeval.problem.Problem;

import java.io.Serializable;
import java.util.List;

/**
 * Created by konstantinberkow on 5/9/17.
 */
public class MainDEResult implements Serializable {

    private final List<double[]> population;
    private final float amplification;
    private final float convergence;
    private final Problem problem;

    public MainDEResult(List<double[]> population, float amplification, float convergense, Problem problem) {
        this.population = population;
        this.amplification = amplification;
        this.convergence = convergense;
        this.problem = problem;
    }

    public List<double[]> getPopulation() {
        return population;
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

    public double value() {
        double value = 0;
        for (double[] vector : population) {
            value += problem.calculate(vector);
        }
        return value / population.size();
    }

    @Override
    public String toString() {
        return "MainDEResult{" +
                "population=" + population +
                ", amplification=" + amplification +
                ", convergence=" + convergence +
                ", problem=" + problem +
                '}';
    }
}
