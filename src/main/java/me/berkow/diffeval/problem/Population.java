package me.berkow.diffeval.problem;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by konstantinberkow on 5/11/17.
 */
public class Population implements Serializable {

    private final Member[] members;

    public Population(Member[] members) {
        this.members = members;
    }

    public Member[] getMembers() {
        return members;
    }

    public int size() {
        return members.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Population that = (Population) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(members, that.members);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(members);
    }

    @Override
    public String toString() {
        return "Population{" +
                "members=" + Arrays.toString(members) +
                '}';
    }
}
