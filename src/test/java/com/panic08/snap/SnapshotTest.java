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

package com.panic08.snap;

import com.esotericsoftware.kryo.Kryo;
import com.panic08.snap.strategy.KryoSnapshotStrategy;
import org.junit.jupiter.api.BeforeEach;
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

    private Kryo kryo;

    @BeforeEach
    void setUp() {
        Kryo newKryo = new Kryo();
        newKryo.setRegistrationRequired(false);
        this.kryo = newKryo;
    }

    @Test
    void testSnapshotStoresDeepCopy() {
        DummyState original = new DummyState("data");
        Snapshot<DummyState> snapshot = new Snapshot<>(original, new KryoSnapshotStrategy<>(kryo));
        original.setData("modified");
        assertEquals("data", snapshot.getState().getData());
    }

    @Test
    void testRestore() {
        DummyState original = new DummyState("initial");
        DummyState target = new DummyState("empty");
        Snapshot<DummyState> snapshot = new Snapshot<>(original, new KryoSnapshotStrategy<>(kryo));
        snapshot.restore(target);
        assertEquals("initial", target.getData());
    }
}
