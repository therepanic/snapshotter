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

package com.panic08.snapshotter;

import com.panic08.snapshotter.event.SnapshotRemovedEvent;
import com.panic08.snapshotter.event.SnapshotRestoredEvent;
import com.panic08.snapshotter.event.SnapshotSavedEvent;

import java.util.List;
import java.util.Map;

public class DefaultSnapshotter<T> implements Snapshotter<T> {

    private final T target;
    private final SnapshotStorage<T> storage;
    private final SnapshotStrategy<T> strategy;
    private final List<SnapshotterListener<T>> listeners;

    public DefaultSnapshotter(T target, SnapshotStorage<T> storage, SnapshotStrategy<T> strategy, List<SnapshotterListener<T>> listeners) {
        this.target = target;
        this.storage = storage;
        this.strategy = strategy;
        this.listeners = listeners;
    }

    @Override
    public void save() {
        save("default");
    }

    @Override
    public void save(String name) {
        Snapshot<T> snapshot = new Snapshot<>(target, strategy);
        storage.save(name, snapshot);
        notify(new SnapshotSavedEvent<>(name, target, snapshot));
    }

    @Override
    public boolean restore() {
        return restore("default");
    }

    @Override
    public boolean restore(String name) {
        Snapshot<T> snapshot = storage.load(name);
        if (snapshot == null) {
            return false;
        }
        snapshot.restore(target);
        notify(new SnapshotRestoredEvent<>(name, target));
        return true;
    }

    @Override
    public boolean restoreLast() {
        Map.Entry<String, Snapshot<T>> snapshotEntry = storage.loadLastEntry();
        if (snapshotEntry.getValue() == null) {
            return false;
        }
        snapshotEntry.getValue().restore(target);
        notify(new SnapshotRestoredEvent<>(snapshotEntry.getKey(), target));
        return true;
    }

    @Override
    public Map<String, String> diff(String name) {
        return DiffUtils.diff(target, storage.load(name).getState());
    }

    @Override
    public Map<String, String> diff() {
        return diff("default");
    }

    @Override
    public Map<String, String> diff(String name1, String name2) {
        return DiffUtils.diff(storage.load(name2).getState(), storage.load(name1).getState());
    }

    @Override
    public boolean hasSnapshot(String name) {
        return storage.hasSnapshot(name);
    }

    @Override
    public void clear() {
        storage.clear();
    }

    @Override
    public void remove() {
        remove("default");
    }

    @Override
    public void remove(String name) {
        storage.remove(name);
        notify(new SnapshotRemovedEvent<>(name, target));
    }

    @Override
    public void runAndSave(Runnable action) {
        action.run();
        save();
    }

    @Override
    public void runAndSave(Runnable action, String name) {
        action.run();
        save(name);
    }

    @Override
    public SnapshotterSchedulerBuilder<T> schedule() {
        return new SnapshotterSchedulerBuilder<>(this);
    }

    @Override
    public void addListener(SnapshotterListener<T> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(SnapshotterListener<T> listener) {
        listeners.remove(listener);
    }

    private void notify(AbstractSnapshotEvent<T> event) {
        for (SnapshotterListener<T> listener : listeners) {
            listener.onEvent(event);
        }
    }

}
