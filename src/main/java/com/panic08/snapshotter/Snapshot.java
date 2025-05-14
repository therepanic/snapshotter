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

import java.lang.reflect.Field;

public class Snapshot<T> {

    private final T state;

    public Snapshot(T state, SnapshotStrategy<T> strategy) {
        this.state = strategy.deepClone(state);
    }

    public Snapshot(T state) {
        this.state = state;
    }

    public Snapshot() {
        this.state = null;
    }

    public void restore(T target) {
        try {
            for (Field field : state.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                field.set(target, field.get(state));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public T getState() {
        return state;
    }

}
