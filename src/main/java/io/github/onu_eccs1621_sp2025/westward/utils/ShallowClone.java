package io.github.onu_eccs1621_sp2025.westward.utils;

/**
 * Allows for cloning only parts of a class.
 * This does create a new Object instead of calling the clone method.
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @since 1.0.0 Alpha 1
 * @version 1.0
 * @param <T> The type of class that is being cloned
 */
public interface ShallowClone<T> {
    /**
     * Clones certain object values
     * @return A copy of only important data from an object
     */
    T shallowClone();
}
