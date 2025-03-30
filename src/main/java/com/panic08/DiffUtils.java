package com.panic08;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DiffUtils {

    public static <T> Map<String, String> diff(T first, T second) {
        if (first == null || second == null) {
            return new HashMap<>();
        }
        return compareObjects("", first, second);
    }

    private static Map<String, String> compareObjects(String prefix, Object first, Object second) {
        Map<String, String> differences = new HashMap<>();
        if (first == null || second == null) {
            String key = prefix.isEmpty() ? "root" : prefix.substring(0, prefix.length() - 1);
            differences.put(key, second + " -> " + first);
            return differences;
        }
        if (first.getClass() != second.getClass()) {
            differences.put(prefix.isEmpty() ? "root" : prefix, second + " -> " + first);
            return differences;
        }
        for (Field field : first.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object firstValue = field.get(first);
                Object secondValue = field.get(second);
                if (!Objects.equals(firstValue, secondValue)) {
                    if (isPrimitiveOrWrapper(field.getType())) {
                        differences.put(prefix + field.getName(), secondValue + " -> " + firstValue);
                    } else {
                        differences.putAll(compareObjects(prefix + field.getName() + ".", firstValue, secondValue));
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return differences;
    }

    private static boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive() || type == String.class || type == Integer.class || type == Double.class ||
                type == Boolean.class || type == Character.class || type == Byte.class || type == Short.class ||
                type == Long.class || type == Float.class;
    }

}
