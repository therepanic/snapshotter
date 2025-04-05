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

package com.panic08;

import com.esotericsoftware.kryo.Kryo;
import com.panic08.storage.BytesFileSnapshotStorage;
import com.panic08.storage.MemorySnapshotStorage;

import java.util.Map;

public final class Snap<T> {

    private final T target;
    private final SnapshotStorage<T> storage;
    private final Kryo kryo;

    private Snap(T target, SnapshotStorage<T> storage, Kryo kryo) {
        this.target = target;
        this.storage = storage;
        this.kryo = kryo;
    }

    public static <T> Snap<T> of(T target) {
        Kryo newKryo = new Kryo();
        newKryo.setRegistrationRequired(false);
        return new Snap<>(target, new MemorySnapshotStorage<>(), newKryo);
    }

    public static <T> Snap<T> of(T target, Kryo kryo) {
        return new Snap<>(target, new MemorySnapshotStorage<>(), kryo);
    }

    public static <T> Snap<T> of(T target, SnapshotStorage<T> storage) {
        Kryo newKryo = new Kryo();
        newKryo.setRegistrationRequired(false);
        return new Snap<>(target, storage, newKryo);
    }

    public static <T> Snap<T> ofBytes(T target) {
        Kryo newKryo = new Kryo();
        newKryo.setRegistrationRequired(false);
        return new Snap<>(target, new BytesFileSnapshotStorage<>(newKryo), newKryo);
    }

    public static <T> Snap<T> of(T target, SnapshotStorage<T> storage, Kryo kryo) {
        return new Snap<>(target, storage, kryo);
    }

    public void save() {
        storage.save("default", new Snapshot<>(target, kryo));
    }

    public void save(String name) {
        storage.save(name, new Snapshot<>(target, kryo));
    }

    public boolean restore() {
        Snapshot<T> snapshot = storage.load("default");
        if (snapshot == null) {
            return false;
        }
        snapshot.restore(target);
        return true;
    }

    public boolean restore(String name) {
        Snapshot<T> snapshot = storage.load(name);
        if (snapshot == null) {
            return false;
        }
        snapshot.restore(target);
        return true;
    }

    public boolean restoreLast() {
        Snapshot<T> snapshot = storage.loadLast();
        if (snapshot == null) {
            return false;
        }
        snapshot.restore(target);
        return true;
    }

    public Map<String, String> diff(String name) {
        return DiffUtils.diff(target, storage.load(name).getState());
    }

    public Map<String, String> diff() {
        return DiffUtils.diff(target, storage.load("default").getState());
    }

    public boolean hasSnapshot(String name) {
        return storage.hasSnapshot(name);
    }

    public void clear() {
        storage.clear();
    }

    public void remove() {
        storage.remove("default");
    }

    public void remove(String name) {
        storage.remove(name);
    }

    public void runAndSave(Runnable action) {
        action.run();
        storage.save("default", new Snapshot<>(target, kryo));
    }

    public void runAndSave(Runnable action, String name) {
        action.run();
        storage.save(name, new Snapshot<>(target, kryo));
    }
}
