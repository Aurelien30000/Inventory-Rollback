package me.danjono.inventoryrollback.util;

import java.math.BigDecimal;

public class MathUtils {

    public static double squared(final double num) {
        return num * num;
    }

    public static long squared(final long num) {
        return num * num;
    }

    public static int squared(final int num) {
        return num * num;
    }

    public static double[] roots(double a, double b, double c) {
        final double rhs = Math.sqrt(b * b - 4 * a * c);
        return new double[]{(-b + rhs) / (2 * a), (-b - rhs) / (2 * a)};
    }

    public static double round(double d, int decimalPlace) {
        return BigDecimal.valueOf(d).setScale(decimalPlace, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    }

    public static float round(float d, int decimalPlace) {
        return BigDecimal.valueOf(d).setScale(decimalPlace, BigDecimal.ROUND_HALF_DOWN).floatValue();
    }

}