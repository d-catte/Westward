package io.github.onu_eccs1621_sp2025.westward.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utilities for getting random elements from a List
 * @author Dylan Catte
 * @since 1.0.0 Alpha 1
 * @version 1.0
 */
public final class ListUtils {
    /**
     * Gets a random element from the specified list
     * @param collection Collection to query
     * @return Random element from the provided list
     */
    public static Object getRandomElement(final Collection<?> collection) {
        final List<?> list = new ArrayList<>(collection);
        if (!list.isEmpty()) {
            final int index = ThreadLocalRandom.current().nextInt(0, list.size());
            return list.get(index);
        }
        return null;
    }
}
