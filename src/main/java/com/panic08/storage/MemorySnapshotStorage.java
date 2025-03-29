package com.panic08.storage;

import com.panic08.Snapshot;
import com.panic08.SnapshotStorage;

import java.util.LinkedHashMap;
import java.util.Map;

public class MemorySnapshotStorage<T> implements SnapshotStorage<T> {

    private final Map<String, Snapshot<T>> snapshots = new LinkedHashMap<>();

    @Override
    public void save(String name, Snapshot<T> snapshot) {
        snapshots.put(name, snapshot);
    }

    @Override
    public Snapshot<T> load(String name) {
        return snapshots.get(name);
    }

    @Override
    public Snapshot<T> loadLast() {
        return snapshots.values().stream().reduce((first, second) -> second).orElse(null);
    }

    @Override
    public boolean hasSnapshot(String name) {
        return snapshots.containsKey(name);
    }

}
