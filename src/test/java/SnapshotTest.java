import com.panic08.Snapshot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SnapshotTest {

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

        public void setData(String data) {
            this.data = data;
        }
    }

    @Test
    void testSnapshotStoresDeepCopy() {
        DummyState original = new DummyState("data");
        Snapshot<DummyState> snapshot = new Snapshot<>(original);
        original.setData("modified");
        assertEquals("data", snapshot.getState().getData());
    }

    @Test
    void testRestore() {
        DummyState original = new DummyState("initial");
        DummyState target = new DummyState("empty");
        Snapshot<DummyState> snapshot = new Snapshot<>(original);
        snapshot.restore(target);
        assertEquals("initial", target.getData());
    }
}
