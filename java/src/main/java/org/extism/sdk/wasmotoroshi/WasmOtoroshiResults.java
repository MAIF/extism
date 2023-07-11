package org.extism.sdk.wasmotoroshi;

public class WasmOtoroshiResults extends WasmOtoroshiParameters implements AutoCloseable {

    public WasmOtoroshiResults(int length) {
        super(length);
    }

    public WasmOtoroshiResults(WasmBridge.ExtismVal.ByReference ptr, int length) {
        super(ptr, length);
    }
}
