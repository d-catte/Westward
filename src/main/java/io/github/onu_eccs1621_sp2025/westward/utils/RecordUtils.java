package io.github.onu_eccs1621_sp2025.westward.utils;

import io.github.onu_eccs1621_sp2025.westward.TrailApplication;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;

/**
 * Tools for improving the functionality of Records
 * @author Dylan Catte
 * @since 1.0.0 Alpha 1
 * @version 1.0
 */
public final class RecordUtils {
    /**
     * Gets the arguments in the constructor of a Record as an Array
     * @param record The record class being iterated through
     * @return An array of the Objects inside the record
     */
    public static Object[] getComponents(final Record record) {
        final Class<?> recordClass = record.getClass();
        final int length = recordClass.getRecordComponents().length;
        final Object[] components = new Object[length];
        for (int i = 0; i < length; i++) {
            final RecordComponent component = recordClass.getRecordComponents()[i];
            try {
                final Method method = recordClass.getMethod(component.getName());
                final Object value = method.invoke(record);
                components[i] = value;
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                DebugLogger.error("Failed to get component [{}]", component.getName(), e);
                throw new RuntimeException(e);
            }
        }
        return components;
    }
}
