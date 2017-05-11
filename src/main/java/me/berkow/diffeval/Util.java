package me.berkow.diffeval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by konstantinberkow on 5/11/17.
 */
public final class Util {

    public static int[] selectIndexes(int from, int to, int except, int quantity, Random random) {
        final List<Integer> allowedInts = new ArrayList<>();

        for (int i = from; i < to; i++) {
            if (i != except) allowedInts.add(i);
        }

        final int[] ints = new int[quantity];

        Collections.shuffle(allowedInts, random);

        for (int i = 0; i < quantity; i++) {
            ints[i] = allowedInts.get(i);
        }

        return ints;
    }

    public static double nextDouble(double min, double max, Random random) {
        return min + (max - min) * random.nextDouble();
    }
}
