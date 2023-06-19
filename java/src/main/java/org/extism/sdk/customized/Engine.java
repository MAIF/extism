package org.extism.sdk.customized;

import com.sun.jna.PointerType;

public class Engine extends PointerType implements AutoCloseable {

    public Engine() {
        super(Bridge.INSTANCE.create_wasmtime_engine());
    }

    public void free() {
        Bridge.INSTANCE.free_engine(this);
    }

    @Override
    public void close() {
        free();
    }
}
