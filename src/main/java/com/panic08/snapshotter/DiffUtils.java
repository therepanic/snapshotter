/*
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.panic08.snapshotter;

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
