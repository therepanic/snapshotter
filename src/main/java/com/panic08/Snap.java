package com.panic08;

import com.panic08.storage.MemorySnapshotStorage;

import java.util.Map;

public final class Snap<T> {

    private final T target;
    private final SnapshotStorage<T> storage;

    private Snap(T target, SnapshotStorage<T> storage) {
        this.target = target;
        this.storage = storage;
    }

    public static <T> Snap<T> of(T target) {
        return new Snap<>(target, new MemorySnapshotStorage<>());
    }

    public static <T> Snap<T> of(T target, SnapshotStorage<T> storage) {
        return new Snap<>(target, storage);
    }

    public void save() {
        storage.save("default", new Snapshot<>(target));
    }

    public void save(String name) {
        storage.save(name, new Snapshot<>(target));
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

}
