package me.berkow.diffeval;

import java.util.*;

/**
 * Created by konstantinberkow on 5/11/17.
 */
@SuppressWarnings("SameParameterValue")
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

    public static int getIntOrDefault(Map<String, String> map, String key, int defaultValue) {
        final String value = map.get(key);
        if (value == null) {
            return defaultValue;
        }

        int trueValue = defaultValue;

        try {
            trueValue = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return trueValue;
    }

    public static long getLongOrDefault(Map<String, String> map, String key, long defaultValue) {
        final String value = map.get(key);
        if (value == null) {
            return defaultValue;
        }

        long trueValue = defaultValue;

        try {
            trueValue = Long.parseLong(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return trueValue;
    }

    public static double getDoubleOrDefault(Map<String, String> map, String key, double defaultValue) {
        final String value = map.get(key);
        if (value == null) {
            return defaultValue;
        }

        double trueValue = defaultValue;

        try {
            trueValue = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return trueValue;
    }

    public static float getFloatOrDefault(Map<String, String> map, String key, float defaultValue) {
        final String value = map.get(key);
        if (value == null) {
            return defaultValue;
        }

        float trueValue = defaultValue;

        try {
            trueValue = Float.parseFloat(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return trueValue;
    }
}
