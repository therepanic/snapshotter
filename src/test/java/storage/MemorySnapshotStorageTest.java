package storage;

import com.panic08.Snapshot;
import com.panic08.storage.MemorySnapshotStorage;
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

    @BeforeEach
    void setUp() {
        storage = new MemorySnapshotStorage<>();
    }

    @Test
    void testSaveAndLoad() {
        DummyState state = new DummyState("data");
        Snapshot<DummyState> snapshot = new Snapshot<>(state);
        storage.save("snapshot1", snapshot);
        assertNotNull(storage.load("snapshot1"));
        assertEquals("data", storage.load("snapshot1").getState().getData());
    }

    @Test
    void testLoadLast() {
        storage.save("first", new Snapshot<>(new DummyState("one")));
        storage.save("second", new Snapshot<>(new DummyState("two")));
        assertEquals("two", storage.loadLast().getState().getData());
    }

    @Test
    void testHasSnapshot() {
        storage.save("exists", new Snapshot<>(new DummyState("value")));
        assertTrue(storage.hasSnapshot("exists"));
        assertFalse(storage.hasSnapshot("missing"));
    }

    @Test
    void testRemove() {
        storage.save("temp", new Snapshot<>(new DummyState("to delete")));
        storage.remove("temp");
        assertFalse(storage.hasSnapshot("temp"));
    }

    @Test
    void testClear() {
        storage.save("one", new Snapshot<>(new DummyState("1")));
        storage.save("two", new Snapshot<>(new DummyState("2")));
        storage.clear();
        assertNull(storage.loadLast());
    }
}

