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

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SnapshotterSchedulerBuilderTest {
    static class Dummy {
        int x;
    }

    @Test
    void throwsIfIntervalNotSet() {
        Snapshotter<Dummy> snap = Snapshotter.of(new Dummy());

        assertThrows(IllegalStateException.class, () -> snap.schedule().build());
    }

    @Test
    void buildsSchedulerWithCorrectDefaults() {
        Snapshotter<Dummy> snap = Snapshotter.of(new Dummy());

        SnapshotterScheduler<Dummy> scheduler = snap.schedule()
                .every(Duration.ofMillis(100))
                .build();

        assertNotNull(scheduler);

        scheduler.start();

        assertTrue(scheduler.isRunning());
        scheduler.stopAll();
        assertFalse(scheduler.isRunning());
    }

    @Test
    void usesCustomNameGenerator() throws InterruptedException {
        Dummy obj = new Dummy();
        obj.x = 100;

        Snapshotter<Dummy> snap = Snapshotter.of(obj);

        SnapshotterScheduler<Dummy> scheduler = snap.schedule()
                .every(Duration.ofMillis(100))
                .saveAs("custom-name")
                .build();

        scheduler.start();
        TimeUnit.MILLISECONDS.sleep(150);
        obj.x = 123;
        scheduler.stopAll();

        snap.restore("custom-name");

        assertEquals(100, obj.x);
    }
}
