package org.extism.sdk.otoroshi;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import org.extism.sdk.manifest.Manifest;
import org.extism.sdk.support.JsonSerde;

import java.nio.charset.StandardCharsets;

public class OtoroshiTemplate extends PointerType implements AutoCloseable {

    private final String id;

    private OtoroshiTemplate(OtoroshiEngine engine, String id, byte[] wasm) {
        super(Bridge.INSTANCE.otoroshi_create_template_new(engine, wasm, wasm.length));
        this.id = id;
    }

    public OtoroshiTemplate(OtoroshiEngine engine, String id, Manifest manifest) {
        this(engine, id, serialize(manifest));
    }

    public OtoroshiTemplate() {
        id = "unknown";
    }

    private static byte[] serialize(Manifest manifest) {
        return JsonSerde.toJson(manifest).getBytes(StandardCharsets.UTF_8);
    }

    public void free() {
        Bridge.INSTANCE.otoroshi_free_template(this);
    }

    public String getId() {
        return id;
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
