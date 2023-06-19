package org.extism.sdk.customized;

import com.sun.jna.PointerType;

public class Engine extends PointerType implements AutoCloseable {

    public Engine() {
        super(Bridge.INSTANCE.create_wasmtime_engine());
    }

    @Override
    public void close() {
        Bridge.INSTANCE.free_engine(this);
    }
}
