package com.panic08;

import com.esotericsoftware.kryo.Kryo;

public class KryoSingleton {

    private static final Kryo kryo = new Kryo();

    static {
        kryo.setRegistrationRequired(false);
    }

    public static Kryo getInstance() {
        return kryo;
    }
}
