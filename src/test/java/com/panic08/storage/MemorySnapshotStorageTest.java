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

package com.panic08.storage;

import com.esotericsoftware.kryo.Kryo;
import com.panic08.Snapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MemorySnapshotStorageTest {

    static class DummyState {
        private String data;

        public DummyState() {
        }

        public DummyState(String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }
    }

    private MemorySnapshotStorage<DummyState> storage;
    private Kryo kryo;

    @BeforeEach
    void setUp() {
        Kryo newKryo = new Kryo();
        newKryo.setRegistrationRequired(false);
        this.kryo = newKryo;
        this.storage = new MemorySnapshotStorage<>();
    }

    @Test
    void testSaveAndLoad() {
        DummyState state = new DummyState("data");
        Snapshot<DummyState> snapshot = new Snapshot<>(state, kryo);
        storage.save("snapshot1", snapshot);
        assertNotNull(storage.load("snapshot1"));
        assertEquals("data", storage.load("snapshot1").getState().getData());
    }

    @Test
    void testLoadLast() {
        storage.save("first", new Snapshot<>(new DummyState("one"), kryo));
        storage.save("second", new Snapshot<>(new DummyState("two"), kryo));
        assertEquals("two", storage.loadLast().getState().getData());
    }

    @Test
    void testHasSnapshot() {
        storage.save("exists", new Snapshot<>(new DummyState("value"), kryo));
        assertTrue(storage.hasSnapshot("exists"));
        assertFalse(storage.hasSnapshot("missing"));
    }

    @Test
    void testRemove() {
        storage.save("temp", new Snapshot<>(new DummyState("to delete"), kryo));
        storage.remove("temp");
        assertFalse(storage.hasSnapshot("temp"));
    }

    @Test
    void testClear() {
        storage.save("one", new Snapshot<>(new DummyState("1"), kryo));
        storage.save("two", new Snapshot<>(new DummyState("2"), kryo));
        storage.clear();
        assertNull(storage.loadLast());
    }
}

