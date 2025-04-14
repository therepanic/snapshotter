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

package com.panic08.snap.strategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.panic08.snap.SnapshotStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class KryoSnapshotStrategy<T> implements SnapshotStrategy<T> {

    private final Kryo kryo;

    public KryoSnapshotStrategy(Kryo kryo) {
        this.kryo = kryo;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T deepClone(T obj) {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        Output output = new Output(byteOutput);
        kryo.writeObject(output, obj);
        output.close();
        ByteArrayInputStream byteInput = new ByteArrayInputStream(byteOutput.toByteArray());
        Input input = new Input(byteInput);
        T cloned = (T) kryo.readObject(input, obj.getClass());
        input.close();
        return cloned;
    }

}
