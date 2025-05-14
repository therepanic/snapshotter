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

package com.panic08.snapshotter.strategy;

import com.esotericsoftware.kryo.Kryo;
import com.panic08.snapshotter.SnapshotStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class KryoSnapshotStrategyTest {

    private Kryo kryo;
    private SnapshotStrategy<Object> strategy;

    @BeforeEach
    public void setUp() {
        Kryo newKryo = new Kryo();
        newKryo.setRegistrationRequired(false);
        this.kryo = newKryo;
        this.strategy = new KryoSnapshotStrategy<>(this.kryo);
    }

    @Test
    void testDeepClonePrimitiveType() {
        //not Integer pool
        Integer original = 400;
        Integer cloned = (Integer) strategy.deepClone(original);
        assertEquals(original, cloned);
        assertNotSame(original, cloned);
    }

    @Test
    void testDeepCloneString() {
        String original = "hello";
        String cloned = (String) strategy.deepClone(original);
        assertEquals(original, cloned);
        assertNotSame(original, cloned);
    }

    @Test
    void testDeepCloneList() {
        List<String> original = new ArrayList<>();
        original.add("one");
        original.add("two");
        List<String> cloned = (List<String>) strategy.deepClone(original);
        assertEquals(original, cloned);
        assertNotSame(original, cloned);
        assertNotSame(original.get(0), cloned.get(0));
    }

    @Test
    void testDeepCloneObject() {
        TestObject original = new TestObject("value", 42);
        TestObject cloned = (TestObject) strategy.deepClone(original);
        assertEquals(original.getValue(), cloned.getValue());
        assertEquals(original.getNumber(), cloned.getNumber());
        assertNotSame(original, cloned);
    }

    static class TestObject {
        private String value;
        private int number;

        public TestObject() {
        }

        public TestObject(String value, int number) {
            this.value = value;
            this.number = number;
        }

        public String getValue() {
            return value;
        }

        public int getNumber() {
            return number;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestObject that = (TestObject) o;
            if (number != that.number) return false;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            int result = value != null ? value.hashCode() : 0;
            result = 31 * result + number;
            return result;
        }
    }
}
