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

package com.therepanic.snapshotter.storage;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.therepanic.snapshotter.AbstractFileSnapshotStorage;
import com.therepanic.snapshotter.Snapshot;

import java.io.File;
import java.util.LinkedHashMap;

public class BytesFileSnapshotStorage<T> extends AbstractFileSnapshotStorage<T> {

	private final Kryo kryo;

	public BytesFileSnapshotStorage(LinkedHashMap<String, File> snapshots, Kryo kryo) {
		super(snapshots);
		this.kryo = kryo;
	}

	public BytesFileSnapshotStorage(Kryo kryo) {
		this(new LinkedHashMap<>(), kryo);
	}

	public BytesFileSnapshotStorage() {
		this(new LinkedHashMap<>(), createDefaultKryo());
	}

	public BytesFileSnapshotStorage(LinkedHashMap<String, File> snapshots) {
		this(snapshots, createDefaultKryo());
	}

	private static Kryo createDefaultKryo() {
		Kryo kryo = new Kryo();
		kryo.setRegistrationRequired(false);
		return kryo;
	}

	@Override
	public byte[] encode(Snapshot<T> snapshot) {
		try (Output output = new Output(1024, -1)) {
			this.kryo.writeObject(output, snapshot);
			return output.toBytes();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Snapshot<T> decode(byte[] data) {
		try (Input input = new Input(data)) {
			return (Snapshot<T>) this.kryo.readObject(input, Snapshot.class);
		}
	}

}
