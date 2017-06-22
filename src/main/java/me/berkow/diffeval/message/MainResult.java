package me.berkow.diffeval.message;

import me.berkow.diffeval.problem.Population;
import me.berkow.diffeval.problem.Problem;

/**
 * Created by konstantinberkow on 6/6/17.
 */
public class MainResult {
    private final Population population;
    private final float amplification;
    private final float crossoverProbability;
    private final Problem problem;
    private final float value;
    private final String type;
    private final int iterationsCount;

    public MainResult(SubResult result, String type, int iterationsCount) {
        this.population = result.getPopulation();
        this.amplification = result.getAmplification();
        this.crossoverProbability = result.getCrossoverProbability();
        this.problem = result.getProblem();
        this.value = result.getValue();
        this.type = type;
        this.iterationsCount = iterationsCount;
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

    public int getIterationsCount() {
        return iterationsCount;
    }

    @Override
    public String toString() {
        return "MainResult{" +
                "population=" + population +
                ", amplification=" + amplification +
                ", crossoverProbability=" + crossoverProbability +
                ", problem=" + problem +
                ", value=" + value +
                ", type='" + type + '\'' +
                ", iterationsCount=" + iterationsCount +
                '}';
    }
}
