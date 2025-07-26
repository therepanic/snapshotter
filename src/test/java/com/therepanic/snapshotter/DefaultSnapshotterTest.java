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

package com.therepanic.snapshotter;

import com.esotericsoftware.kryo.Kryo;
import com.therepanic.snapshotter.event.SnapshotterRestoredEvent;
import com.therepanic.snapshotter.event.SnapshotterSavedEvent;
import com.therepanic.snapshotter.storage.MemorySnapshotStorage;
import com.therepanic.snapshotter.strategy.KryoSnapshotStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class DefaultSnapshotterTest {

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

	private DefaultSnapshotter<DummyState> snapshotter;

	@BeforeEach
	void setUp() {
		target = new DummyState("initial");
		Kryo newKryo = new Kryo();
		newKryo.setRegistrationRequired(false);
		snapshotter = new DefaultSnapshotter<>(target, new MemorySnapshotStorage<>(),
				new KryoSnapshotStrategy<>(newKryo), new ArrayList<>());
	}

	@Test
	void testSaveAndRestoreDefault() {
		snapshotter.save();
		target.setData("modified");
		boolean restored = snapshotter.restore();
		assertTrue(restored);
		assertEquals("initial", target.getData());
	}

	@Test
	void testSaveAndRestoreNamed() {
		snapshotter.save("snap1");
		target.setData("changed");
		boolean restored = snapshotter.restore("snap1");
		assertTrue(restored);
		assertEquals("initial", target.getData());
	}

	@Test
	void testRestoreLast() {
		snapshotter.save("first");
		target.setData("second");
		snapshotter.save("second");
		target.setData("third");
		boolean restored = snapshotter.restoreLast();
		assertTrue(restored);
		assertEquals("second", target.getData());
	}

	@Test
	void testDiffNoDifference() {
		snapshotter.save();
		Map<String, String> diff = snapshotter.diff();
		assertTrue(diff.isEmpty());
	}

	@Test
	void testDiffWithNameNoDifference() {
		snapshotter.save("snap");
		Map<String, String> diff = snapshotter.diff("snap");
		assertTrue(diff.isEmpty());
	}

	@Test
	void testDiffWithDifference() {
		snapshotter.save();
		target.setData("changed");
		Map<String, String> diff = snapshotter.diff();
		assertFalse(diff.isEmpty());
		assertEquals("initial -> changed", diff.get("data"));
	}

	@Test
	void testDiffBetweenTwoSnapshots() {
		snapshotter.save("snap1");
		target.setData("first");
		snapshotter.save("snap2");
		Map<String, String> diff = snapshotter.diff("snap1", "snap2");
		assertFalse(diff.isEmpty());
		assertEquals("initial -> first", diff.get("data"));
	}

	@Test
	void testHasSnapshot() {
		snapshotter.save();
		assertTrue(snapshotter.hasSnapshot("default"));
		assertFalse(snapshotter.hasSnapshot("nonexistent"));
	}

	@Test
	void testClear() {
		snapshotter.save();
		snapshotter.clear();
		boolean restored = snapshotter.restore();
		assertFalse(restored);
	}

	@Test
	void testRemoveDefault() {
		snapshotter.save();
		snapshotter.remove();
		assertFalse(snapshotter.hasSnapshot("default"));
	}

	@Test
	void testRemoveNamed() {
		snapshotter.save("snapX");
		snapshotter.remove("snapX");
		assertFalse(snapshotter.hasSnapshot("snapX"));
	}

	@Test
	void testRunAndSaveDefault() {
		snapshotter.save();
		target.setData("changed");
		snapshotter.runAndSave(() -> target.setData("runAndSave"));
		target.setData("modified");
		boolean restored = snapshotter.restore();
		assertTrue(restored);
		assertEquals("runAndSave", target.getData());
	}

	@Test
	void testRunAndSaveNamed() {
		snapshotter.save();
		target.setData("changed");
		snapshotter.runAndSave(() -> target.setData("runAndSaveNamed"), "named");
		target.setData("modified");
		boolean restored = snapshotter.restore("named");
		assertTrue(restored);
		assertEquals("runAndSaveNamed", target.getData());
	}

	@Test
	void listenerShouldBeNotifiedOnSaveAndRestoreWithDetails() {
		target.setData("initial");

		AtomicBoolean saveCalled = new AtomicBoolean(false);
		AtomicBoolean restoreCalled = new AtomicBoolean(false);

		snapshotter.addListener(event -> {
			if (event instanceof SnapshotterSavedEvent) {
				saveCalled.set(true);
				assertEquals("default", event.getName());
				assertEquals("initial", event.getTarget().getData());
				assertNotNull(((SnapshotterSavedEvent<DummyState>) event).getSnapshot());
			}
			else if (event instanceof SnapshotterRestoredEvent) {
				restoreCalled.set(true);
				assertEquals("default", event.getName());
				assertEquals("initial", event.getTarget().getData());
			}
		});

		snapshotter.save();
		assertTrue(saveCalled.get());

		target.setData("changed");
		snapshotter.restore();
		assertTrue(restoreCalled.get());
		assertEquals("initial", target.getData());
	}

}