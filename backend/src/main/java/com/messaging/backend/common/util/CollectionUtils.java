package com.messaging.backend.common.util;

import java.util.Collection;

/**
 * Generic collection manipulation utilities.
 * 
 * Intended usage:
 * Provides safe standard collection checks avoiding null pointer exceptions.
 * 
 * Future extension points:
 * Extensible for mapping aggregations or strict generic filtering later.
 */
public final class CollectionUtils {

    private CollectionUtils() {
        // Prevent instantiation
    }

    /**
     * Checks if a collection is null or empty.
     * 
     * @param collection the collection to check
     * @return true if null or empty, false otherwise
     */
    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}
