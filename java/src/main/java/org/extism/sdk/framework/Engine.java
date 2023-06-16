package org.extism.sdk.framework;

import com.sun.jna.PointerType;

public class Engine extends PointerType implements AutoCloseable {

    public Engine() {
        super(NewFramework.INSTANCE.create_wasmtime_engine());
    }

    @Override
    public void close() {
        NewFramework.INSTANCE.free_engine(this);
    }
}
