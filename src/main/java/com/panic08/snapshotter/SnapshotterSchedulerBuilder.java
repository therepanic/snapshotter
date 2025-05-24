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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class SnapshotterSchedulerBuilder<T> {

    private final Snapshotter<T> snapshotter;
    private ScheduledExecutorService scheduler;
    private Duration interval;
    private BooleanSupplier condition;
    private Supplier<String> nameGenerator;
    private ConcurrentLinkedQueue<Future<?>> container;
    private Duration initialDelay;

    public SnapshotterSchedulerBuilder(Snapshotter<T> snapshotter) {
        this.snapshotter = snapshotter;
    }

    public SnapshotterSchedulerBuilder<T> every(Duration interval) {
        this.interval = interval;
        return this;
    }

    public SnapshotterSchedulerBuilder<T> when(BooleanSupplier condition) {
        this.condition = condition;
        return this;
    }

    public SnapshotterSchedulerBuilder<T> saveAs(Supplier<String> nameGenerator) {
        this.nameGenerator = nameGenerator;
        return this;
    }

    public SnapshotterSchedulerBuilder<T> saveAs(String name) {
        return saveAs(() -> name);
    }

    public SnapshotterSchedulerBuilder<T> scheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
        this.scheduler = scheduledExecutorService;
        return this;
    }

    public SnapshotterSchedulerBuilder<T> container(ConcurrentLinkedQueue<Future<?>> container) {
        this.container = container;
        return this;
    }

    public SnapshotterSchedulerBuilder<T> initialDelay(Duration initialDelay) {
        this.initialDelay = initialDelay;
        return this;
    }

    public SnapshotterScheduler<T> build() {
        if (this.interval == null) {
            throw new IllegalStateException("Interval must be set");
        }
        if (this.condition == null) {
            this.condition = () -> true;
        }
        if (this.nameGenerator == null) {
            this.nameGenerator = () -> "default";
        }
        if (this.container == null) {
            this.container = new ConcurrentLinkedQueue<>();
        }
        if (this.initialDelay == null) {
            this.initialDelay = Duration.ofMillis(0);
        }
        if (scheduler == null) {
            return new SnapshotterScheduler<>(this.snapshotter, this.interval, this.condition, this.nameGenerator, this.initialDelay, this.container);
        } else {
            return new SnapshotterScheduler<>(this.scheduler, this.snapshotter, this.interval, this.condition, this.nameGenerator, this.initialDelay, this.container);
        }
    }

}
