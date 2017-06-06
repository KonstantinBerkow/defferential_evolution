package me.berkow.diffeval.message;

import me.berkow.diffeval.problem.Population;
import me.berkow.diffeval.problem.Problem;

/**
 * Created by konstantinberkow on 6/6/17.
 */
public class MainDEResult {
    private final Population population;
    private final float amplification;
    private final float crossoverProbability;
    private final Problem problem;
    private final double value;
    private final String type;

    public MainDEResult(DEResult result, String type) {
        this.population = result.getPopulation();
        this.amplification = result.getAmplification();
        this.crossoverProbability = result.getCrossoverProbability();
        this.problem = result.getProblem();
        this.value = result.getValue();
        this.type = type;
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

    public Problem getProblem() {
        return problem;
    }

    public double getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "MainDEResult{" +
                "population=" + population +
                ", amplification=" + amplification +
                ", crossoverProbability=" + crossoverProbability +
                ", problem=" + problem +
                ", value=" + value +
                ", type='" + type + '\'' +
                '}';
    }
}
