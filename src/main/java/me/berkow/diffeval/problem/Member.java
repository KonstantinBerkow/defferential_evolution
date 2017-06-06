package me.berkow.diffeval.problem;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by konstantinberkow on 5/11/17.
 */
public class Member implements Serializable {

    private final float[] innerVector;

    public Member(float[] innerVector) {
        this.innerVector = innerVector;
    }

    public float get(int position) {
        return innerVector[position];
    }

    public int size() {
        return innerVector.length;
    }

    public float[] toArray() {
        return innerVector;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Member member = (Member) o;

        return Arrays.equals(innerVector, member.innerVector);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(innerVector);
    }

    @Override
    public String toString() {
        return Arrays.toString(innerVector);
    }
}
