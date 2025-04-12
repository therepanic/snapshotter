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

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class SnapScheduler<T> implements AutoCloseable {

    private final ScheduledExecutorService scheduler;
    private final boolean ownsScheduler;
    private final Snap<T> snap;
    private final Duration interval;
    private final BooleanSupplier condition;
    private final Supplier<String> nameGenerator;
    private ScheduledFuture<?> task;

    public SnapScheduler(ScheduledExecutorService scheduler, Snap<T> snap, Duration interval, BooleanSupplier condition, Supplier<String> nameGenerator) {
        this.scheduler = scheduler;
        this.ownsScheduler = false;
        this.snap = snap;
        this.interval = interval;
        this.condition = condition;
        this.nameGenerator = nameGenerator;
    }

    public SnapScheduler(Snap<T> snap, Duration interval, BooleanSupplier condition, Supplier<String> nameGenerator) {
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.ownsScheduler = true;
        this.snap = snap;
        this.interval = interval;
        this.condition = condition;
        this.nameGenerator = nameGenerator;
    }

    public void start() {
        if (task != null) {
            throw new IllegalStateException("Scheduler is already running");
        }
        task = scheduler.scheduleAtFixedRate(() -> {
            if (condition.getAsBoolean()) {
                snap.save(nameGenerator.get());
            }
        }, 0, interval.toMillis(), TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (task == null) {
            throw new IllegalStateException("Scheduler is not running");
        }
        task.cancel(false);
        task = null;
    }

    public boolean isRunning() {
        return task != null && !task.isDone();
    }

    @Override
    public void close() throws Exception {
        if (task != null) {
            task.cancel(false);
            task = null;
        }
        if (ownsScheduler) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
