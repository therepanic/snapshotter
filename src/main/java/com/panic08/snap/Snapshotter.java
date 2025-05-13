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
import com.panic08.snap.storage.MemorySnapshotStorage;
import com.panic08.snap.strategy.KryoSnapshotStrategy;

import java.util.ArrayList;
import java.util.Map;

public interface Snapshotter<T> {

    static <T> DefaultSnapshotter<T> of(T target) {
        Kryo newKryo = new Kryo();
        newKryo.setRegistrationRequired(false);
        return new DefaultSnapshotter<>(target, new MemorySnapshotStorage<>(), new KryoSnapshotStrategy<>(newKryo), new ArrayList<>());
    }

    static <T> DefaultSnapshotter<T> of(T target, SnapshotStrategy<T> strategy) {
        return new DefaultSnapshotter<>(target, new MemorySnapshotStorage<>(), strategy, new ArrayList<>());
    }

    static <T> DefaultSnapshotter<T> of(T target, SnapshotStorage<T> storage) {
        Kryo newKryo = new Kryo();
        newKryo.setRegistrationRequired(false);
        return new DefaultSnapshotter<>(target, storage, new KryoSnapshotStrategy<>(newKryo), new ArrayList<>());
    }

    static <T> DefaultSnapshotter<T> of(T target, SnapshotStorage<T> storage, SnapshotStrategy<T> strategy) {
        return new DefaultSnapshotter<>(target, storage, strategy, new ArrayList<>());
    }

    void save();

    void save(String name);

    boolean restore();

    boolean restore(String name);

    boolean restoreLast();

    Map<String, String> diff();

    Map<String, String> diff(String name);

    Map<String, String> diff(String name1, String name2);

    boolean hasSnapshot(String name);

    void clear();

    void remove();

    void remove(String name);

    void runAndSave(Runnable action);

    void runAndSave(Runnable action, String name);

    SnapshotterSchedulerBuilder<T> schedule();

    void addListener(SnapshotterListener<T> listener);

    void removeListener(SnapshotterListener<T> listener);

}