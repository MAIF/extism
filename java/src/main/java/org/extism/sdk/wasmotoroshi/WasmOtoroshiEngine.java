package org.extism.sdk.wasmotoroshi;

import com.sun.jna.PointerType;

public class WasmOtoroshiEngine extends PointerType implements AutoCloseable {

    public WasmOtoroshiEngine() {
        super(WasmBridge.INSTANCE.wasm_otoroshi_create_wasmtime_engine());
    }

    public void free() {
        WasmBridge.INSTANCE.wasm_otoroshi_free_engine(this);
    }

    @Override
    public void close() {
        free();
    }
}
