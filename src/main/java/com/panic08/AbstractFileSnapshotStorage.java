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

import com.panic08.Snapshot;
import com.panic08.SnapshotStorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractFileSnapshotStorage<T> implements SnapshotStorage<T> {

    private final Map<String, File> snapshots = new LinkedHashMap<>();

    protected abstract byte[] encode(Snapshot<T> snapshot);

    protected abstract Snapshot<T> decode(byte[] data);

    @Override
    public void save(String name, Snapshot<T> snapshot) {
        byte[] data = encode(snapshot);
        File newFile = new File(name);
        try (FileOutputStream fos = new FileOutputStream(newFile)) {
            fos.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        snapshots.put(name, newFile);
    }

    @Override
    public Snapshot<T> load(String name) {
        File file = new File(name);
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            return decode(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Snapshot<T> loadLast() {
        String lastPath = snapshots.keySet().stream().reduce((first, second) -> second).orElse(null);
        if (lastPath == null) {
            return null;
        }
        return load(lastPath);
    }

    @Override
    public boolean hasSnapshot(String name) {
        return snapshots.containsKey(name);
    }

    @Override
    public void clear() {
        for (File file : snapshots.values()) {
            file.delete();
        }
        snapshots.clear();
    }

    @Override
    public void remove(String name) {
        snapshots.get(name).delete();
        snapshots.remove(name);
    }

}
