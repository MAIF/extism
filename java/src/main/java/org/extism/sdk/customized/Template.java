package org.extism.sdk.customized;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import org.extism.sdk.HostFunction;
import org.extism.sdk.LinearMemory;
import org.extism.sdk.manifest.Manifest;
import org.extism.sdk.support.JsonSerde;

import java.nio.charset.StandardCharsets;

public class Template extends PointerType implements AutoCloseable {

    private Template(Engine engine,  byte[] wasm) {
        super(Bridge.INSTANCE.create_template_new(engine, wasm, wasm.length));
    }

    public Template(Engine engine, Manifest manifest) {
        this(engine, serialize(manifest));
    }

    public Template() {

    }

    private static byte[] serialize(Manifest manifest) {
        return JsonSerde.toJson(manifest).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void close() throws Exception {
        Bridge.INSTANCE.free_template(this);
    }

    public Instance instantiate(Engine engine, HostFunction[] functions, LinearMemory[] memories, boolean withWasi) {
        Pointer[] functionsPtr = HostFunction.arrayToPointer(functions);
        Memory[] memoriesPtr = LinearMemory.arrayToPointer(memories);

        return Bridge.INSTANCE.instantiate(
                engine,
                this,
                functionsPtr.length == 0 ? null : functionsPtr,
                functionsPtr.length,
                memoriesPtr.length == 0 ? null : memoriesPtr,
                memoriesPtr.length,
                withWasi);
    }
}
