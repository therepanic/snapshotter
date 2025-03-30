import com.panic08.DiffUtils;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiffUtilsTest {

    static class SimpleObject {
        private final int number;
        private final String text;

        public SimpleObject(int number, String text) {
            this.number = number;
            this.text = text;
        }
    }

    static class Address {
        private final String city;
        private final int zip;

        public Address(String city, int zip) {
            this.city = city;
            this.zip = zip;
        }

        @Override
        public String toString() {
            return "Address[city=" + city + ", zip=" + zip + "]";
        }
    }

    static class ComplexObject {
        private final String name;
        private final Address address;
        public ComplexObject(String name, Address address) {
            this.name = name;
            this.address = address;
        }
    }

    @Test
    void testDiffWithNullFirst() {
        SimpleObject second = new SimpleObject(42, "test");
        Map<String, String> result = DiffUtils.diff(null, second);
        assertEquals(new HashMap<>(), result);
    }

    @Test
    void testDiffWithNullSecond() {
        SimpleObject first = new SimpleObject(42, "test");
        Map<String, String> result = DiffUtils.diff(first, null);
        assertEquals(new HashMap<>(), result);
    }

    @Test
    void testDiffWithIdenticalObjects() {
        SimpleObject first = new SimpleObject(42, "test");
        SimpleObject second = new SimpleObject(42, "test");
        Map<String, String> result = DiffUtils.diff(first, second);
        assertTrue(result.isEmpty());
    }

    @Test
    void testDiffWithPrimitiveFields() {
        SimpleObject first = new SimpleObject(42, "test");
        SimpleObject second = new SimpleObject(100, "changed");
        Map<String, String> result = DiffUtils.diff(first, second);
        Map<String, String> expected = new HashMap<>();
        expected.put("number", "100 -> 42");
        expected.put("text", "changed -> test");
        assertEquals(expected, result);
    }

    @Test
    void testDiffWithNestedObjects() {
        Address addr1 = new Address("Moscow", 123);
        Address addr2 = new Address("Berlin", 456);
        ComplexObject first = new ComplexObject("Alice", addr1);
        ComplexObject second = new ComplexObject("Bob", addr2);
        Map<String, String> result = DiffUtils.diff(first, second);
        Map<String, String> expected = new HashMap<>();
        expected.put("name", "Bob -> Alice");
        expected.put("address.city", "Berlin -> Moscow");
        expected.put("address.zip", "456 -> 123");
        assertEquals(expected, result);
    }

    @Test
    void testDiffWithNullNestedField() {
        ComplexObject first = new ComplexObject("Alice", null);
        ComplexObject second = new ComplexObject("Alice", new Address("Berlin", 456));
        Map<String, String> result = DiffUtils.diff(first, second);
        Map<String, String> expected = new HashMap<>();
        expected.put("address", "Address[city=Berlin, zip=456] -> null");
        assertEquals(expected, result);
    }

    @Test
    void testDiffWithDifferentClasses() {
        SimpleObject first = new SimpleObject(42, "test");
        ComplexObject second = new ComplexObject("Bob", new Address("Berlin", 456));
        Map<String, String> result = DiffUtils.diff(first, second);
        Map<String, String> expected = new HashMap<>();
        expected.put("root", second + " -> " + first);
        assertEquals(expected, result);
    }

    @Test
    void testDiffWithNoDifferencesInNested() {
        Address addr = new Address("Moscow", 123);
        ComplexObject first = new ComplexObject("Alice", addr);
        ComplexObject second = new ComplexObject("Alice", addr);
        Map<String, String> result = DiffUtils.diff(first, second);
        assertTrue(result.isEmpty());
    }

}
