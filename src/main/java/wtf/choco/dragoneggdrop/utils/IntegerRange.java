package wtf.choco.dragoneggdrop.utils;

import java.util.Random;

import com.google.common.base.Preconditions;

/**
 * Represents a pair of integer values denoting a minimum and maximum.
 *
 * @author Parker Hawke
 */
public final class IntegerRange {

    private final int min;
    private final int max;

    private IntegerRange(int min, int max) {
        Preconditions.checkArgument(min <= max, "min must be <= max");

        this.min = min;
        this.max = max;
    }

    /**
     * Get the minimum value in this range.
     *
     * @return the minimum value
     */
    public int getMin() {
        return min;
    }

    /**
     * Get the maximum value in this range.
     *
     * @return the maximum value
     */
    public int getMax() {
        return max;
    }

    /**
     * Get a random integer between the minimum and maximum value of this range
     * (both inclusive).
     *
     * @param random a random instance
     *
     * @return the randomly generated value between this range
     */
    public int getRandomValue(Random random) {
        return min + random.nextInt(max - min + 1);
    }

    /**
     * Create an IntegerRange between two values.
     *
     * @param first the first value
     * @param second the second value
     *
     * @return the integer range
     */
    public static IntegerRange between(int first, int second) {
        return new IntegerRange(Math.min(first, second), Math.max(first, second));
    }

    /**
     * Create an IntegerRange where the min and max values are set to the same integer.
     *
     * @param value the value to set
     *
     * @return the integer range
     */
    public static IntegerRange only(int value) {
        return new IntegerRange(value, value);
    }

}
