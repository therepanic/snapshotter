package com.panic08;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.lang.reflect.Field;

public class Snapshot<T> {

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

    public String show() {
        return state.toString();
    }

    private T deepCopy(T obj) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                 ObjectInputStream ois = new ObjectInputStream(bis)) {
                return (T) ois.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public T getState() {
        return state;
    }

}
