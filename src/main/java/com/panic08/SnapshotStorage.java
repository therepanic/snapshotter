package com.panic08;

public interface SnapshotStorage<T> {

    void save(String name, Snapshot<T> snapshot);

    Snapshot<T> load(String name);

    Snapshot<T> loadLast();

    boolean hasSnapshot(String name);

}
