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

package com.panic08.snapshotter.storage;

import com.panic08.snapshotter.Snapshot;
import com.panic08.snapshotter.SnapshotStorage;

import java.util.LinkedHashMap;
import java.util.Map;

public class MemorySnapshotStorage<T> implements SnapshotStorage<T> {

    private final LinkedHashMap<String, Snapshot<T>> snapshots;

    public MemorySnapshotStorage(LinkedHashMap<String, Snapshot<T>> snapshots) {
        this.snapshots = snapshots;
    }

    public MemorySnapshotStorage() {
        this(new LinkedHashMap<>());
    }

    @Override
    public void save(String name, Snapshot<T> snapshot) {
        this.snapshots.put(name, snapshot);
    }

    @Override
    public Snapshot<T> load(String name) {
        return this.snapshots.get(name);
    }

    @Override
    public Map.Entry<String, Snapshot<T>> loadLastEntry() {
        return this.snapshots.entrySet().stream().reduce((first, second) -> second).orElse(null);
    }

    @Override
    public boolean hasSnapshot(String name) {
        return this.snapshots.containsKey(name);
    }

    @Override
    public void clear() {
        this.snapshots.clear();
    }

    @Override
    public void remove(String name) {
        this.snapshots.remove(name);
    }

}
