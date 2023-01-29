package me.dinozoid.strife.util.system;

import net.minecraft.util.MathHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public final class MathUtil {

    public static double getDistance(double srcX, double srcZ, double dstX, double dstZ) {
        double xDiff = dstX - srcX;
        double zDiff = dstZ - srcZ;
        return MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff);
    }

    public static float calculateGaussianValue(float x, float sigma) {
        double PI = Math.PI;
        double output = 1.0 / Math.sqrt(2.0 * PI * (sigma * sigma));
        return (float) (output * Math.exp(-(x * x) / (2.0 * (sigma * sigma))));
    }

    public static double tryParseDouble(String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static double getDifference(double base, double yaw) {
        final double bigger;
        if (base >= yaw)
            bigger = base - yaw;
        else
            bigger = yaw - base;
        return bigger;
    }

    public static double tryParseFloat(String value, float defaultValue) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean tryParseBoolean(String value, boolean defaultValue) {
        try {
            return Boolean.parseBoolean(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();

        return new BigDecimal(value)
                .setScale(places, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public static double round(double value, int places, double increment) {
        if (places < 0)
            throw new IllegalArgumentException();

        final double flooredValue = Math.floor(value / increment) * increment;
        final double ceiledValue = Math.ceil(value / increment) * increment;
        final boolean aboveHalfIncrement = value >= flooredValue + (increment / 2.0);
        return new BigDecimal(aboveHalfIncrement ? ceiledValue : flooredValue)
                .setScale(places, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public static float round(float value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();

        return new BigDecimal(value)
                .setScale(places, RoundingMode.HALF_UP)
                .floatValue();
    }

    public static float round(float value, int places, float increment) {
        if (places < 0)
            throw new IllegalArgumentException();
        final double flooredValue = Math.floor(value / increment) * increment;
        final double ceiledValue = Math.ceil(value / increment) * increment;
        final boolean aboveHalfIncrement = value >= flooredValue + (increment / 2.0);
        return new BigDecimal(aboveHalfIncrement ? ceiledValue : flooredValue)
                .setScale(places, RoundingMode.HALF_UP)
                .floatValue();
    }

    public static double randomDouble(double min, double max) {
        if(min > max) return min;
        return new Random().nextDouble() * (max - min) + min;
    }

    public static float randomFloat(float min, float max) {
        if(min > max) return min;
        return new Random().nextFloat() * (max - min) + min;
    }

    public static long randomLong(long min, long max) {
        if(min > max) return min;
        return new Random().nextLong() * (max - min) + min;
    }

    public static int randomInt(int min, int max) {
        if(min > max) return min;
        return new Random().nextInt(max) + min;
    }
    public static byte randomByte(byte min, byte max) {
        if(min > max) return min;
        return (byte) (new Random().nextInt(max) + min);
    }
    public static boolean randomBoolean() {
        return randomBoolean(1, 0.5);
    }
    public static boolean randomBoolean(double range, double value) {
        return randomDouble(0, range) > value;
    }

    public static byte[] randomBytes(int minSize, int maxSize, byte min, byte max) {
        int size = randomInt(minSize, maxSize);
        final byte[] out = new byte[size];
        for (int i = 0; i < size; i++) {
            out[i] = randomByte(min, max);
        }
        return out;
    }

}
