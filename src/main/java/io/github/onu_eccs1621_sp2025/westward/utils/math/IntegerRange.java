package io.github.onu_eccs1621_sp2025.westward.utils.math;

import java.util.OptionalInt;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Randomly selects a value from a given range of integers.
 * The random can either be traditional or gaussian.
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @since 1.0.0 Alpha 1
 * @version 1.1
 * @param min Minimum inclusive value
 * @param max Maximum inclusive value
 * @param normal Whether to use gaussian distribution or traditional distribution
 * @param mean Optional mean of Gaussian distribution
 * @param stdDev The Optional standard deviation
 */
public record IntegerRange(int min, int max, boolean normal, Double mean, Double stdDev) {
    /**
     * Generates a random int using the appropriate distribution.
     * @return random int in the specified range
     */
    public int random() {
        if (this.min == this.max) {
            return this.min;
        }
        if (normal) {
            if (this.mean == null || this.stdDev == null) {
                final double rngVal = ThreadLocalRandom.current().nextGaussian(0, 1);
                return (int) Math.round(rngVal * (this.max - this.min) + this.min);
            } else {
                final double rngVal = ThreadLocalRandom.current().nextGaussian(this.mean, this.stdDev);
                return (int) Math.round(rngVal);
            }
        } else {
            return ThreadLocalRandom.current().nextInt(this.min, this.max + 1);
        }
    }

    /**
     * Generates an optional int using the appropriate distribution.
     * The weight determines of a value should be generated.
     * @param weight probability that the value should be generated
     * @return An int or empty
     */
    public OptionalInt optionalRandom(final float weight) {
        return ThreadLocalRandom.current().nextFloat() <= weight ? OptionalInt.of(this.random()) : OptionalInt.empty();
    }
}
