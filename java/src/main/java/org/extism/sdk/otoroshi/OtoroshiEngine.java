package org.extism.sdk.otoroshi;

import com.sun.jna.PointerType;

public class OtoroshiEngine extends PointerType implements AutoCloseable {

    public OtoroshiEngine() {
        super(Bridge.INSTANCE.otoroshi_create_wasmtime_engine());
    }

    public void free() {
        Bridge.INSTANCE.otoroshi_free_engine(this);
    }

    @Override
    public void close() {
        free();
    }
}
