package org.extism.sdk.wasmotoroshi;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import org.extism.sdk.manifest.Manifest;
import org.extism.sdk.support.JsonSerde;

import java.nio.charset.StandardCharsets;

public class WasmOtoroshiTemplate extends PointerType implements AutoCloseable {

    private final String id;

    private WasmOtoroshiTemplate(WasmOtoroshiEngine engine, String id, byte[] wasm) {
        super(WasmBridge.INSTANCE.wasm_otoroshi_create_template_new(engine, wasm, wasm.length));
        this.id = id;
    }

    public WasmOtoroshiTemplate(WasmOtoroshiEngine engine, String id, Manifest manifest) {
        this(engine, id, serialize(manifest));
    }

    public WasmOtoroshiTemplate() {
        id = "unknown";
    }

    private static byte[] serialize(Manifest manifest) {
        return JsonSerde.toJson(manifest).getBytes(StandardCharsets.UTF_8);
    }

    public void free() {
        WasmBridge.INSTANCE.wasm_otoroshi_free_template(this);
    }

    public String getId() {
        return id;
    }

    @Override
    public void close() {
        free();
    }

    public WasmOtoroshiInstance instantiate(WasmOtoroshiEngine engine, WasmOtoroshiHostFunction[] functions, WasmOtoroshiLinearMemory[] memories, boolean withWasi) {
        Pointer[] functionsPtr = WasmOtoroshiHostFunction.arrayToPointer(functions);
        WasmOtoroshiMemory[] memoriesPtr = WasmOtoroshiLinearMemory.arrayToPointer(memories);

        return WasmBridge.INSTANCE.wasm_otoroshi_instantiate(
                engine,
                this,
                functionsPtr.length == 0 ? null : functionsPtr,
                functionsPtr.length,
                memoriesPtr.length == 0 ? null : memoriesPtr,
                memoriesPtr.length,
                withWasi);
    }
}
