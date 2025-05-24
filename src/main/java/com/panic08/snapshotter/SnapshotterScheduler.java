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

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class SnapshotterScheduler<T> implements AutoCloseable {

    private final ScheduledExecutorService scheduler;
    private final boolean ownsScheduler;
    private final Snapshotter<T> snapshotter;
    private final Duration interval;
    private final BooleanSupplier condition;
    private final Supplier<String> nameGenerator;
    private final Duration initialDelay;
    private final List<Future<?>> tasks;

    private SnapshotterScheduler(ScheduledExecutorService scheduler, boolean ownsScheduler, Snapshotter<T> snapshotter, Duration interval, BooleanSupplier condition, Supplier<String> nameGenerator, Duration initialDelay, List<Future<?>> tasks) {
        this.scheduler = scheduler;
        this.ownsScheduler = ownsScheduler;
        this.snapshotter = snapshotter;
        this.interval = interval;
        this.condition = condition;
        this.nameGenerator = nameGenerator;
        this.initialDelay = initialDelay;
        this.tasks = tasks;
    }

    public SnapshotterScheduler(ScheduledExecutorService scheduler, Snapshotter<T> snapshotter, Duration interval, BooleanSupplier condition, Supplier<String> nameGenerator, Duration initialDelay, List<Future<?>> tasks) {
        this(scheduler, false, snapshotter, interval, condition, nameGenerator, initialDelay, tasks);
    }

    public SnapshotterScheduler(Snapshotter<T> snapshotter, Duration interval, BooleanSupplier condition, Supplier<String> nameGenerator, Duration initialDelay, List<Future<?>> tasks) {
        this(defaultScheduler(), true, snapshotter, interval, condition, nameGenerator, initialDelay, tasks);
    }

    public static ScheduledExecutorService defaultScheduler() {
        return Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void start() {
        Future<?> task = this.scheduler.scheduleAtFixedRate(() -> {
            if (this.condition.getAsBoolean()) {
                synchronized (this.snapshotter) {
                    this.snapshotter.save(this.nameGenerator.get());
                }
            }
        }, this.initialDelay.toMillis(), this.interval.toMillis(), TimeUnit.MILLISECONDS);
        this.tasks.add(task);
    }

    public void stopAll() {
        for (Future<?> task : this.tasks) {
            task.cancel(false);
        }
        this.tasks.clear();
    }

    public boolean isRunning() {
        return !this.tasks.isEmpty();
    }

    @Override
    public void close() throws Exception {
        stopAll();
        if (this.ownsScheduler) {
            this.scheduler.shutdown();
            try {
                if (!this.scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    this.scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
