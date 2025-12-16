package io.github.onu_eccs1621_sp2025.westward.utils;

import io.github.onu_eccs1621_sp2025.westward.data.Date;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Calculates the current weather
 * @author Dylan Catte
 * @since 1.0.0 Alpha 2
 * @version 1.0
 */
public final class WeatherHelper {
    /**
     * The temperature of the current day in F
     */
    private static int temperature;

    /**
     * Gets the day's temperature
     * @return The temperature for today in F
     */
    public static int getTemp() {
        return temperature;
    }

    /**
     * Calculates the current temperature for the day with respect to the current month
     * @param month The current month
     */
    public static void forecastTemperature(final Date.Month month) {
        temperature = switch (month) {
            // Winter
            case NOV, DEC, JAN, FEB -> ThreadLocalRandom.current().nextInt(0, 45);
            case MAR, APR -> ThreadLocalRandom.current().nextInt(20, 60);
            case MAY, JUN, JUL, AUG, SEP -> ThreadLocalRandom.current().nextInt(55, 90);
            case OCT -> ThreadLocalRandom.current().nextInt(30, 65);
            default -> 0;
        };
    }
}
