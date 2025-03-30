import com.panic08.Snap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SnapTest {

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

    private DummyState target;
    private Snap<DummyState> snap;

    @BeforeEach
    void setUp() {
        target = new DummyState("initial");
        snap = Snap.of(target);
    }

    @Test
    void testSaveAndRestoreDefault() {
        snap.save();
        target.setData("modified");
        boolean restored = snap.restore();
        assertTrue(restored);
        assertEquals("initial", target.getData());
    }

    @Test
    void testSaveAndRestoreNamed() {
        snap.save("snap1");
        target.setData("changed");
        boolean restored = snap.restore("snap1");
        assertTrue(restored);
        assertEquals("initial", target.getData());
    }

    @Test
    void testRestoreLast() {
        snap.save("first");
        target.setData("second");
        snap.save("second");
        target.setData("third");
        boolean restored = snap.restoreLast();
        assertTrue(restored);
        assertEquals("second", target.getData());
    }

    @Test
    void testDiffNoDifference() {
        snap.save();
        Map<String, String> diff = snap.diff();
        assertTrue(diff.isEmpty());
    }

    @Test
    void testDiffWithDifference() {
        snap.save();
        target.setData("changed");
        Map<String, String> diff = snap.diff();
        assertFalse(diff.isEmpty());
        assertEquals("initial -> changed", diff.get("data"));
    }

    @Test
    void testHasSnapshot() {
        snap.save();
        assertTrue(snap.hasSnapshot("default"));
        assertFalse(snap.hasSnapshot("nonexistent"));
    }

    @Test
    void testClear() {
        snap.save();
        snap.clear();
        boolean restored = snap.restore();
        assertFalse(restored);
    }

    @Test
    void testRemoveDefault() {
        snap.save();
        snap.remove();
        assertFalse(snap.hasSnapshot("default"));
    }

    @Test
    void testRemoveNamed() {
        snap.save("snapX");
        snap.remove("snapX");
        assertFalse(snap.hasSnapshot("snapX"));
    }

    @Test
    void testRunAndSaveDefault() {
        snap.save();
        target.setData("changed");
        snap.runAndSave(() -> target.setData("runAndSave"));
        target.setData("modified");
        boolean restored = snap.restore();
        assertTrue(restored);
        assertEquals("runAndSave", target.getData());
    }

    @Test
    void testRunAndSaveNamed() {
        snap.save();
        target.setData("changed");
        snap.runAndSave(() -> target.setData("runAndSaveNamed"), "named");
        target.setData("modified");
        boolean restored = snap.restore("named");
        assertTrue(restored);
        assertEquals("runAndSaveNamed", target.getData());
    }
}