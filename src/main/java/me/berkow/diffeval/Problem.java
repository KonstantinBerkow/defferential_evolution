package me.berkow.diffeval;

import me.berkow.diffeval.problem.Member;

import java.io.Serializable;

/**
 * Created by konstantinberkow on 5/11/17.
 */
public abstract class Problem implements Serializable {

    private final int id;
    private final float[] lowerConstraints;
    private final float[] upperConstraints;

    public Problem(int id, float[] lowerConstraints, float[] upperConstraints) {
        this.id = id;
        this.lowerConstraints = lowerConstraints;
        this.upperConstraints = upperConstraints;
    }

    public float[] getLowerConstraints() {
        return lowerConstraints;
    }

    public float[] getUpperConstraints() {
        return upperConstraints;
    }

    public int getSize() {
        return lowerConstraints.length;
    }

    public int getId() {
        return id;
    }

    public abstract float calculate(Member vector);
}
