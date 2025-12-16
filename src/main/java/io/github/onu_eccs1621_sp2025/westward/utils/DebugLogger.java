package io.github.onu_eccs1621_sp2025.westward.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prints debug messages in the console if debugMode is enabled
 */
public final class DebugLogger {
    /**
     * Toggles printing messages to the console
     */
    private static final boolean debugMode = true;
    /**
     * The backend logger by SLF4J
     */
    private static final Logger logger = LoggerFactory.getLogger("Westward");

    /**
     * Prints info to the console
     * @param info Information to print
     */
    public static void info(final String info) {
        if (debugMode) {
            logger.info(info);
        }
    }

    /**
     * Prints info to the console
     * @param info Information to print
     * @param arg1 First arg for formatting
     */
    public static void info(final String info, final Object arg1) {
        if (debugMode) {
            logger.info(info, arg1);
        }
    }

    /**
     * Prints info to the console
     * @param info Information to print
     * @param arg1 First arg for formatting
     * @param arg2 Second arg for formatting
     */
    public static void info(final String info, final Object arg1, final Object arg2) {
        if (debugMode) {
            logger.info(info, arg1, arg2);
        }
    }

    /**
     * Prints warning to the console
     * @param warning Warning to print
     */
    public static void warn(final String warning) {
        if (debugMode) {
            logger.warn(warning);
        }
    }

    /**
     * Prints warning to the console
     * @param warning Warning to print
     * @param arg1 First arg for formatting
     */
    public static void warn(final String warning, final Object arg1) {
        if (debugMode) {
            logger.warn(warning, arg1);
        }
    }

    /**
     * Prints warning to the console
     * @param warning Warning to print
     * @param arg1 First arg for formatting
     * @param arg2 Second arg for formatting
     */
    public static void warn(final String warning, final Object arg1, final Object arg2) {
        if (debugMode) {
            logger.info(warning, arg1, arg2);
        }
    }

    /**
     * Prints an error to the console
     * @param error Error to print
     */
    public static void error(final String error) {
        if (debugMode) {
            logger.error(error);
        }
    }

    /**
     * Prints an error to the console
     * @param error Error to print
     * @param arg1 First arg for formatting
     */
    public static void error(final String error, final Object arg1) {
        if (debugMode) {
            logger.error(error, arg1);
        }
    }

    /**
     * Prints an error to the console
     * @param error Error to print
     * @param arg1 First arg for formatting
     * @param arg2 Second arg for formatting
     */
    public static void error(final String error, final Object arg1, final Object arg2) {
        if (debugMode) {
            logger.error(error, arg1, arg2);
        }
    }
}
