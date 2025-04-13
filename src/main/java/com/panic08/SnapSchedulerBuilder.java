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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class SnapSchedulerBuilder<T> {

    private ScheduledExecutorService scheduler;
    private final Snap<T> snap;
    private Duration interval;
    private BooleanSupplier condition;
    private Supplier<String> nameGenerator;
    private List<Future<?>> container;

    public SnapSchedulerBuilder(Snap<T> snap) {
        this.snap = snap;
    }

    public SnapSchedulerBuilder<T> every(Duration interval) {
        this.interval = interval;
        return this;
    }

    public SnapSchedulerBuilder<T> when(BooleanSupplier condition) {
        this.condition = condition;
        return this;
    }

    public SnapSchedulerBuilder<T> saveAs(Supplier<String> nameGenerator) {
        this.nameGenerator = nameGenerator;
        return this;
    }

    public SnapSchedulerBuilder<T> saveAs(String name) {
        return saveAs(() -> name);
    }

    public SnapSchedulerBuilder<T> scheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
        this.scheduler = scheduledExecutorService;
        return this;
    }

    public SnapSchedulerBuilder<T> container(List<Future<?>> container) {
        this.container = container;
        return this;
    }

    public SnapScheduler<T> build() {
        if (interval == null) {
            throw new IllegalStateException("Interval must be set");
        }
        if (condition == null) {
            condition = () -> true;
        }
        if (nameGenerator == null) {
            nameGenerator = () -> "default";
        }
        if (container == null) {
            container = new ArrayList<>();
        }
        if (scheduler == null) {
            return new SnapScheduler<>(snap, interval, condition, nameGenerator, container);
        } else {
            return new SnapScheduler<>(scheduler, snap, interval, condition, nameGenerator, container);
        }
    }

}
