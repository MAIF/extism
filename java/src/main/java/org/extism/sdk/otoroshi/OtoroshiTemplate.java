package org.extism.sdk.otoroshi;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import org.extism.sdk.manifest.Manifest;
import org.extism.sdk.support.JsonSerde;

import java.nio.charset.StandardCharsets;

public class OtoroshiTemplate extends PointerType implements AutoCloseable {

    private OtoroshiTemplate(OtoroshiEngine engine, byte[] wasm) {
        super(Bridge.INSTANCE.otoroshi_create_template_new(engine, wasm, wasm.length));
    }

    public OtoroshiTemplate(OtoroshiEngine engine, Manifest manifest) {
        this(engine, serialize(manifest));
    }

    public OtoroshiTemplate() {

    }

    private static byte[] serialize(Manifest manifest) {
        return JsonSerde.toJson(manifest).getBytes(StandardCharsets.UTF_8);
    }

    public void free() {
        Bridge.INSTANCE.otoroshi_free_template(this);
    }

    @Override
    public void close() {
        free();
    }

    public OtoroshiInstance instantiate(OtoroshiEngine engine, OtoroshiHostFunction[] functions, OtoroshiLinearMemory[] memories, boolean withWasi) {
        Pointer[] functionsPtr = OtoroshiHostFunction.arrayToPointer(functions);
        OtoroshiMemory[] memoriesPtr = OtoroshiLinearMemory.arrayToPointer(memories);

        return Bridge.INSTANCE.otoroshi_instantiate(
                engine,
                this,
                functionsPtr.length == 0 ? null : functionsPtr,
                functionsPtr.length,
                memoriesPtr.length == 0 ? null : memoriesPtr,
                memoriesPtr.length,
                withWasi);
    }
}
