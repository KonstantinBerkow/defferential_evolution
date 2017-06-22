package me.berkow.diffeval.util;

import java.text.NumberFormat;
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

    public static float[] getFloatArrayOrThrow(Map<String, String> map, String key, String msg) {
        final String value = map.get(key);
        if (value == null) {
            throw new IllegalStateException(msg);
        }

        final String[] rawArr = value.split(",");
        final float[] result = new float[rawArr.length];

        try {
            for (int i = 0; i < rawArr.length; i++) {
                result[i] = Float.parseFloat(rawArr[i]);
            }
        } catch (NumberFormatException e) {
            throw new IllegalStateException(e);
        }

        return result;
    }

    public static String prettyNumber(double value, NumberFormat format) {
        return format.format(value);
    }
}
