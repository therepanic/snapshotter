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

package com.panic08.snap.storage;

import com.esotericsoftware.kryo.Kryo;
import com.panic08.snap.Snapshot;
import com.panic08.snap.AbstractFileSnapshotStorage;
import com.panic08.snap.strategy.KryoSnapshotStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

abstract class AbstractFileSnapshotStorageTest {
    @TempDir
    protected Path tempDir;

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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DummyState that = (DummyState) o;
            return data != null ? data.equals(that.data) : that.data == null;
        }

        @Override
        public int hashCode() {
            return data != null ? data.hashCode() : 0;
        }
    }

    protected Kryo kryo;
    protected AbstractFileSnapshotStorage<DummyState> storage;

    protected abstract AbstractFileSnapshotStorage<DummyState> createStorage();

    @BeforeEach
    void setUp() {
        Kryo newKryo = new Kryo();
        newKryo.setRegistrationRequired(false);
        this.kryo = newKryo;
        this.storage = createStorage();
    }

    @AfterEach
    void tearDown() {
        this.storage.clear();
    }

    @Test
    void testSaveAndLoad() {
        DummyState state = new DummyState("data");
        Snapshot<DummyState> snapshot = new Snapshot<>(state, new KryoSnapshotStrategy<>(kryo));
        String filePath = tempDir.resolve("snapshot1").toString();
        storage.save(filePath, snapshot);

        Snapshot<DummyState> loaded = storage.load(filePath);
        assertNotNull(loaded);
        assertEquals("data", loaded.getState().getData());
    }

    @Test
    void testLoadLastEntry() {
        String firstPath = tempDir.resolve("first").toString();
        String secondPath = tempDir.resolve("second").toString();

        storage.save(firstPath, new Snapshot<>(new DummyState("one"), new KryoSnapshotStrategy<>(kryo)));
        storage.save(secondPath, new Snapshot<>(new DummyState("two"), new KryoSnapshotStrategy<>(kryo)));

        Map.Entry<String, Snapshot<DummyState>> last = storage.loadLastEntry();
        assertNotNull(last);
        assertEquals("two", last.getValue().getState().getData());
    }

    @Test
    void testHasSnapshot() {
        String filePath = tempDir.resolve("exists").toString();
        storage.save(filePath, new Snapshot<>(new DummyState("value"), new KryoSnapshotStrategy<>(kryo)));

        assertTrue(storage.hasSnapshot(filePath));
        assertFalse(storage.hasSnapshot(tempDir.resolve("missing").toString()));
    }

    @Test
    void testRemove() {
        String filePath = tempDir.resolve("temp").toString();
        storage.save(filePath, new Snapshot<>(new DummyState("to delete"), new KryoSnapshotStrategy<>(kryo)));

        storage.remove(filePath);
        assertFalse(storage.hasSnapshot(filePath));
        assertFalse(new File(filePath).exists());
    }

    @Test
    void testClear() {
        String path1 = tempDir.resolve("one").toString();
        String path2 = tempDir.resolve("two").toString();

        storage.save(path1, new Snapshot<>(new DummyState("1"), new KryoSnapshotStrategy<>(kryo)));
        storage.save(path2, new Snapshot<>(new DummyState("2"), new KryoSnapshotStrategy<>(kryo)));

        storage.clear();

        assertNull(storage.loadLastEntry());
        assertFalse(new File(path1).exists());
        assertFalse(new File(path2).exists());
    }
}
