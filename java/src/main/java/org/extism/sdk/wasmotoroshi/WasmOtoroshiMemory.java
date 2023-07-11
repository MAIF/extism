package org.extism.sdk.wasmotoroshi;

import com.sun.jna.PointerType;

public class WasmOtoroshiMemory extends PointerType implements AutoCloseable {

    private String name;
    private String namespace;
    private int minPages;
    private int maxPages;

    public WasmOtoroshiMemory() {}

    public WasmOtoroshiMemory(String name, String namespace, int minPages, int maxPages) {
        super(WasmBridge.INSTANCE.wasm_otoroshi_create_wasmtime_memory(
                name,
                namespace,
                minPages,
                maxPages));

        this.name = name;
        this.namespace = namespace;
        this.minPages = minPages;
        this.maxPages = maxPages;
    }

    @Override
    public void close() {
        WasmBridge.INSTANCE.wasm_otoroshi_free_memory(this);
    }
}
