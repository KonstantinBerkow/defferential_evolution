package me.berkow.diffeval.message;

import me.berkow.diffeval.problem.Problem;

import java.io.Serializable;
import java.util.List;

/**
 * Created by konstantinberkow on 5/10/17.
 */
public class DEResult implements Serializable {
    private final List<double[]> population;
    private final float amplification;
    private final float convergence;
    private final Problem problem;

    private double value = Double.NaN;

    public DEResult(List<double[]> population, float amplification, float convergence, Problem problem) {
        this.population = population;
        this.amplification = amplification;
        this.convergence = convergence;
        this.problem = problem;
    }

    public DEResult(List<double[]> population, float amplification, float convergence, Problem problem, double value) {
        this(population, amplification, convergence, problem);
        this.value = value;
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

    public double getValue() {
        if (Double.isNaN(value)) {
            value = 0;
            for (double[] vector : population) {
                value += problem.calculate(vector);
            }
            value /= population.size();
        }
        return value;
    }
}
