package com.panic08;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;

public class Snapshot<T> {

    private final Kryo kryo = KryoSingleton.getInstance();
    private final T state;

    public Snapshot(T state) {
        this.state = deepCopy(state);
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

    private T deepCopy(T obj) {
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

    public T getState() {
        return state;
    }

}
