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
import com.panic08.snap.event.SnapshotRemovedEvent;
import com.panic08.snap.event.SnapshotRestoredEvent;
import com.panic08.snap.event.SnapshotSavedEvent;
import com.panic08.snap.storage.MemorySnapshotStorage;
import com.panic08.snap.strategy.KryoSnapshotStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Snap<T> {

    private final T target;
    private final SnapshotStorage<T> storage;
    private final SnapshotStrategy<T> strategy;
    private final List<SnapListener<T>> listeners;

    private Snap(T target, SnapshotStorage<T> storage, SnapshotStrategy<T> strategy, List<SnapListener<T>> listeners) {
        this.target = target;
        this.storage = storage;
        this.strategy = strategy;
        this.listeners = listeners;
    }

    public static <T> Snap<T> of(T target) {
        Kryo newKryo = new Kryo();
        newKryo.setRegistrationRequired(false);
        return new Snap<>(target, new MemorySnapshotStorage<>(), new KryoSnapshotStrategy<>(newKryo), new ArrayList<>());
    }

    public static <T> Snap<T> of(T target, SnapshotStrategy<T> strategy) {
        return new Snap<>(target, new MemorySnapshotStorage<>(), strategy, new ArrayList<>());
    }

    public static <T> Snap<T> of(T target, SnapshotStorage<T> storage) {
        Kryo newKryo = new Kryo();
        newKryo.setRegistrationRequired(false);
        return new Snap<>(target, storage, new KryoSnapshotStrategy<>(newKryo), new ArrayList<>());
    }

    public static <T> Snap<T> of(T target, SnapshotStorage<T> storage, SnapshotStrategy<T> strategy) {
        return new Snap<>(target, storage, strategy, new ArrayList<>());
    }

    public void save() {
        save("default");
    }

    public void save(String name) {
        Snapshot<T> snapshot = new Snapshot<>(target, strategy);
        storage.save(name, snapshot);
        notify(new SnapshotSavedEvent<>(name, target, snapshot));
    }

    public boolean restore() {
        return restore("default");
    }

    public boolean restore(String name) {
        Snapshot<T> snapshot = storage.load(name);
        if (snapshot == null) {
            return false;
        }
        snapshot.restore(target);
        notify(new SnapshotRestoredEvent<>(name, target));
        return true;
    }

    public boolean restoreLast() {
        Map.Entry<String, Snapshot<T>> snapshotEntry = storage.loadLastEntry();
        if (snapshotEntry.getValue() == null) {
            return false;
        }
        snapshotEntry.getValue().restore(target);
        notify(new SnapshotRestoredEvent<>(snapshotEntry.getKey(), target));
        return true;
    }

    public Map<String, String> diff(String name) {
        return DiffUtils.diff(target, storage.load(name).getState());
    }

    public Map<String, String> diff() {
        return diff("default");
    }

    public Map<String, String> diff(String name1, String name2) {
        return DiffUtils.diff(storage.load(name2).getState(), storage.load(name1).getState());
    }

    public boolean hasSnapshot(String name) {
        return storage.hasSnapshot(name);
    }

    public void clear() {
        storage.clear();
    }

    public void remove() {
        remove("default");
    }

    public void remove(String name) {
        storage.remove(name);
        notify(new SnapshotRemovedEvent<>(name, target));
    }

    public void runAndSave(Runnable action) {
        action.run();
        save();
    }

    public void runAndSave(Runnable action, String name) {
        action.run();
        save(name);
    }

    public SnapSchedulerBuilder<T> schedule() {
        return new SnapSchedulerBuilder<>(this);
    }

    public void addListener(SnapListener<T> listener) {
        listeners.add(listener);
    }

    public void removeListener(SnapListener<T> listener) {
        listeners.remove(listener);
    }

    private void notify(AbstractSnapshotEvent<T> event) {
        for (SnapListener<T> listener : listeners) {
            listener.onEvent(event);
        }
    }

}
