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

import com.therepanic.snapshotter.event.SnapshotterRemovedEvent;
import com.therepanic.snapshotter.event.SnapshotterRestoredEvent;
import com.therepanic.snapshotter.event.SnapshotterSavedEvent;

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
        Snapshot<T> snapshot = new Snapshot<>(this.target, this.strategy);
        this.storage.save(name, snapshot);
        notify(new SnapshotterSavedEvent<>(name, this.target, snapshot));
    }

    @Override
    public boolean restore() {
        return restore("default");
    }

    @Override
    public boolean restore(String name) {
        Snapshot<T> snapshot = this.storage.load(name);
        if (snapshot == null) {
            return false;
        }
        snapshot.restore(this.target);
        notify(new SnapshotterRestoredEvent<>(name, this.target));
        return true;
    }

    @Override
    public boolean restoreLast() {
        Map.Entry<String, Snapshot<T>> snapshotEntry = this.storage.loadLastEntry();
        if (snapshotEntry.getValue() == null) {
            return false;
        }
        snapshotEntry.getValue().restore(this.target);
        notify(new SnapshotterRestoredEvent<>(snapshotEntry.getKey(), this.target));
        return true;
    }

    @Override
    public Map<String, String> diff(String name) {
        return DiffUtils.diff(this.target, this.storage.load(name).getState());
    }

    @Override
    public Map<String, String> diff() {
        return diff("default");
    }

    @Override
    public Map<String, String> diff(String name1, String name2) {
        return DiffUtils.diff(this.storage.load(name2).getState(), this.storage.load(name1).getState());
    }

    @Override
    public boolean hasSnapshot(String name) {
        return this.storage.hasSnapshot(name);
    }

    @Override
    public void clear() {
        this.storage.clear();
    }

    @Override
    public void remove() {
        remove("default");
    }

    @Override
    public void remove(String name) {
        this.storage.remove(name);
        notify(new SnapshotterRemovedEvent<>(name, this.target));
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
        this.listeners.add(listener);
    }

    @Override
    public void removeListener(SnapshotterListener<T> listener) {
        this.listeners.remove(listener);
    }

    private void notify(AbstractSnapshotterEvent<T> event) {
        for (SnapshotterListener<T> listener : this.listeners) {
            listener.onEvent(event);
        }
    }

}
