package me.berkow.diffeval.message;

import me.berkow.diffeval.problem.Problem;
import me.berkow.diffeval.problem.Problems;

import java.io.Serializable;
import java.util.List;


public class DEResult implements Serializable {
    private final List<double[]> population;
    private final float amplification;
    private final float crossoverProbability;
    private final Problem problem;

    private double value = Double.NaN;

    public DEResult(List<double[]> population, float amplification, float crossoverProbability, Problem problem) {
        this.population = population;
        this.amplification = amplification;
        this.crossoverProbability = crossoverProbability;
        this.problem = problem;
    }

    public DEResult(List<double[]> population, float amplification, float crossoverProbability, Problem problem, double value) {
        this(population, amplification, crossoverProbability, problem);
        this.value = value;
    }

    public List<double[]> getPopulation() {
        return population;
    }

    public float getAmplification() {
        return amplification;
    }

    public float getCrossoverProbability() {
        return crossoverProbability;
    }

    public Problem getProblem() {
        return problem;
    }

    public double getValue() {
        if (Double.isNaN(value)) {
            value = Problems.calculatePopulationValue(problem, population);
        }
        return value;
    }

    @Override
    public String toString() {
        return "DEResult{" +
                "population=" + population +
                ", amplification=" + amplification +
                ", crossoverProbability=" + crossoverProbability +
                ", problem=" + problem +
                ", value=" + value +
                '}';
    }
}
