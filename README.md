# Snapshotter

A lightweight Java library for creating, managing, and restoring object state snapshots. Snapshotter provides a simple API for capturing object state at any point in time and restoring it later, similar to version control but for runtime object state.

[![Java](https://img.shields.io/badge/Java-8+-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE.md)
[![Gradle](https://img.shields.io/badge/Gradle-7.0+-green.svg)](https://gradle.org/)

## Overview

Snapshotter enables you to:
- Save and restore object state at any point
- Compare current state with saved snapshots
- Automatically create snapshots on a schedule
- Track state changes through an event system
- Use multiple storage backends (memory, files, custom)

The library is designed with extensibility in mind, allowing you to customize serialization strategies and storage mechanisms to fit your specific needs.

## Features

- **State Management**: Save and restore object state with named snapshots
- **Multiple Storage Options**: Memory-based, JSON files, or binary files
- **State Comparison**: Built-in diff functionality to compare states
- **Scheduled Snapshots**: Automatic snapshot creation with configurable conditions
- **Event System**: Listen to snapshot operations (save, restore, remove)
- **Extensible Architecture**: Implement custom storage and serialization strategies

## Requirements

- Java 8 or higher
- Gradle 7.0+ (for building from source)

### Dependencies

- `com.esotericsoftware:kryo:5.6.2` - For object serialization
- `com.google.code.gson:gson:2.13.1` - For JSON storage support

## Installation

### Gradle

Add JitPack repository and dependency to your `build.gradle`:

```gradle
repositories {
    maven {
        url 'https://jitpack.io'
    }
}

dependencies {
    implementation 'com.github.therepanic:snapshotter:1.0.0-SNAPSHOT'
}
```

### Maven

Add JitPack repository and dependency to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.therepanic</groupId>
        <artifactId>snapshotter</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

## Quick Start

Basic usage:

```java
MyClass obj = new MyClass();
obj.setValue("initial");

Snapshotter<MyClass> snapshotter = Snapshotter.of(obj);
snapshotter.save();

obj.setValue("modified");
snapshotter.restore();

System.out.println(obj.getValue()); // "initial"
```

## Usage

### Creating a Snapshotter

The library provides several factory methods for creating a `Snapshotter` instance:

```java
// Default: memory storage with Kryo serialization
Snapshotter<MyClass> snap = Snapshotter.of(obj);

// With custom serialization strategy
SnapshotStrategy<MyClass> strategy = new MyCustomStrategy();
Snapshotter<MyClass> snap = Snapshotter.of(obj, strategy);

// With custom storage
SnapshotStorage<MyClass> storage = new JsonFileSnapshotStorage<>(type);
Snapshotter<MyClass> snap = Snapshotter.of(obj, storage);

// Full customization
Snapshotter<MyClass> snap = Snapshotter.of(obj, storage, strategy);
```

### Named Snapshots

Create multiple snapshots with different names:

```java
snapshotter.save("checkpoint-1");
obj.modify();
snapshotter.save("checkpoint-2");
obj.modify();

// Restore a specific snapshot
snapshotter.restore("checkpoint-1");

// Restore the last saved snapshot
snapshotter.restoreLast();
```

### State Comparison

Compare the current state with saved snapshots or compare two snapshots:

```java
snapshotter.save("initial");
obj.setValue("changed");
obj.setCount(42);

// Compare current state with a snapshot
Map<String, String> diff = snapshotter.diff("initial");
// Result: {"value": "initial -> changed", "count": "0 -> 42"}

// Compare two snapshots
snapshotter.save("second");
Map<String, String> diff2 = snapshotter.diff("initial", "second");
```

The diff returns a map where keys are field paths (using dot notation for nested objects) and values are strings showing the change in format "old -> new".

### Snapshot Management

```java
// Check if a snapshot exists
if (snapshotter.hasSnapshot("my-snapshot")) {
    snapshotter.restore("my-snapshot");
}

// Remove a specific snapshot
snapshotter.remove("my-snapshot");

// Remove the default snapshot
snapshotter.remove();

// Clear all snapshots
snapshotter.clear();
```

### Execute and Save

Convenient method to execute code and automatically save the result:

```java
snapshotter.runAndSave(() -> {
    obj.performComplexOperation();
    obj.calculateResults();
}, "after-calculation");
```

## Storage Types

### MemorySnapshotStorage

Stores snapshots in memory. Fast but not persistent across application restarts.

```java
SnapshotStorage<MyClass> storage = new MemorySnapshotStorage<>();
Snapshotter<MyClass> snap = Snapshotter.of(obj, storage);
```

### JsonFileSnapshotStorage

Saves snapshots as JSON files. Useful for debugging and human-readable format.

```java
Type typeOfT = new TypeToken<MyClass>() {}.getType();
SnapshotStorage<MyClass> storage = new JsonFileSnapshotStorage<>(typeOfT);
Snapshotter<MyClass> snap = Snapshotter.of(obj, storage);
```

### BytesFileSnapshotStorage

Saves snapshots in binary format using Kryo. Compact and fast.

```java
SnapshotStorage<MyClass> storage = new BytesFileSnapshotStorage<>();
Snapshotter<MyClass> snap = Snapshotter.of(obj, storage);
```

## Scheduled Snapshots

The library supports automatic snapshot creation on a schedule using `SnapshotterScheduler`.

### Basic Usage

```java
Snapshotter<MyClass> snap = Snapshotter.of(obj);

SnapshotterScheduler<MyClass> scheduler = snap.schedule()
    .every(Duration.ofSeconds(5))
    .build();

scheduler.start();
// Snapshots will be created every 5 seconds

scheduler.stopAll();
```

### Conditional Snapshots

Only create snapshots when a condition is met:

```java
AtomicInteger counter = new AtomicInteger(0);

SnapshotterScheduler<MyClass> scheduler = snap.schedule()
    .every(Duration.ofSeconds(1))
    .when(() -> counter.get() < 10)
    .build();

scheduler.start();
```

### Dynamic Snapshot Names

Generate snapshot names dynamically:

```java
AtomicInteger snapshotNumber = new AtomicInteger(0);

SnapshotterScheduler<MyClass> scheduler = snap.schedule()
    .every(Duration.ofMinutes(1))
    .saveAs(() -> "snapshot-" + snapshotNumber.incrementAndGet())
    .build();

scheduler.start();
```

### Advanced Configuration

```java
ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
ConcurrentLinkedQueue<Future<?>> tasks = new ConcurrentLinkedQueue<>();

SnapshotterScheduler<MyClass> scheduler = snap.schedule()
    .every(Duration.ofSeconds(30))
    .when(() -> obj.isDirty())
    .saveAs(() -> "auto-" + System.currentTimeMillis())
    .scheduledExecutorService(executor)
    .container(tasks)
    .initialDelay(Duration.ofSeconds(5))
    .build();

scheduler.start();

// AutoCloseable support
try (scheduler) {
    // Your code
}
```

The scheduler implements `AutoCloseable` and will automatically stop all tasks and shutdown the executor (if it owns it) when closed.

## Event System

Listen to snapshot operations through the event system:

```java
snapshotter.addListener(event -> {
    if (event instanceof SnapshotterSavedEvent) {
        SnapshotterSavedEvent<MyClass> saveEvent = (SnapshotterSavedEvent<MyClass>) event;
        System.out.println("Saved: " + saveEvent.getName());
        System.out.println("Snapshot: " + saveEvent.getSnapshot());
    } else if (event instanceof SnapshotterRestoredEvent) {
        System.out.println("Restored: " + event.getName());
    } else if (event instanceof SnapshotterRemovedEvent) {
        System.out.println("Removed: " + event.getName());
    }
});

snapshotter.save("test");
snapshotter.restore("test");
snapshotter.remove("test");
```

### Event Types

- **`SnapshotterSavedEvent`** - Fired when a snapshot is saved
- **`SnapshotterRestoredEvent`** - Fired when state is restored from a snapshot
- **`SnapshotterRemovedEvent`** - Fired when a snapshot is removed

## Extending the Library

### Custom Serialization Strategy

Implement `SnapshotStrategy` to provide custom deep cloning logic:

```java
public class MyCustomStrategy<T> implements SnapshotStrategy<T> {
    @Override
    public T deepClone(T obj) {
        // Your deep cloning logic
        return cloneObject(obj);
    }
}
```

### Custom Storage

Implement `SnapshotStorage` to provide custom storage backend:

```java
public class DatabaseSnapshotStorage<T> implements SnapshotStorage<T> {
    @Override
    public void save(String name, Snapshot<T> snapshot) {
        // Save to database
    }
    
    @Override
    public Snapshot<T> load(String name) {
        // Load from database
    }
    
    @Override
    public Map.Entry<String, Snapshot<T>> loadLastEntry() {
        // Return last entry from database
    }
    
    @Override
    public boolean hasSnapshot(String name) {
        // Check if snapshot exists in database
    }
    
    @Override
    public void clear() {
        // Clear all snapshots from database
    }
    
    @Override
    public void remove(String name) {
        // Remove specific snapshot from database
    }
}
```

## Architecture

The library is built using several design patterns:

- **Strategy Pattern** - For serialization (`SnapshotStrategy`)
- **Storage Abstraction** - For different storage backends (`SnapshotStorage`)
- **Builder Pattern** - For scheduler configuration
- **Observer Pattern** - For event system (`SnapshotterListener`)

### Component Structure

```
Snapshotter (interface)
  └── DefaultSnapshotter (implementation)
       ├── SnapshotStorage (storage abstraction)
       │    ├── MemorySnapshotStorage
       │    ├── JsonFileSnapshotStorage
       │    └── BytesFileSnapshotStorage
       ├── SnapshotStrategy (serialization strategy)
       │    └── KryoSnapshotStrategy
       ├── SnapshotterScheduler (automatic snapshots)
       └── SnapshotterListener (event handling)
```

## Examples

### Example 1: Game State Checkpoint

```java
class GameState {
    private int score;
    private int level;
    // ...
}

GameState game = new GameState();
Snapshotter<GameState> snap = Snapshotter.of(game);

game.setScore(1000);
snap.save("checkpoint");

game.setScore(500);
snap.restore("checkpoint");
```

### Example 2: Debugging Complex Calculations

```java
class ComplexCalculation {
    private List<Double> intermediateResults;
    // ...
}

ComplexCalculation calc = new ComplexCalculation();
Snapshotter<ComplexCalculation> snap = Snapshotter.of(calc);

calc.step1();
snap.save("after-step1");

calc.step2();
snap.save("after-step2");

Map<String, String> diff = snap.diff("after-step1", "after-step2");
System.out.println("Changes: " + diff);
```

### Example 3: State Monitoring

```java
class ApplicationState {
    private int userCount;
    private double memoryUsage;
    // ...
}

ApplicationState state = new ApplicationState();
Snapshotter<ApplicationState> snap = Snapshotter.of(state);

snap.addListener(event -> {
    if (event instanceof SnapshotterSavedEvent) {
        log.info("State saved: " + event.getName());
    }
});

snap.schedule()
    .every(Duration.ofMinutes(1))
    .saveAs(() -> "state-" + System.currentTimeMillis())
    .build()
    .start();
```

## Building

To build the project from source:

```bash
./gradlew build
```

## Testing

Run the test suite:

```bash
./gradlew test
```

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

## Author

Andrey Litvitski

## Acknowledgments

- [Kryo](https://github.com/EsotericSoftware/kryo) - Fast serialization framework
- [Gson](https://github.com/google/gson) - JSON library
