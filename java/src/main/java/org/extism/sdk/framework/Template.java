package org.extism.sdk.framework;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import org.extism.sdk.HostFunction;
import org.extism.sdk.LinearMemory;
import org.extism.sdk.manifest.Manifest;
import org.extism.sdk.support.JsonSerde;

import java.nio.charset.StandardCharsets;

public class Template extends PointerType implements AutoCloseable {

    private Template(Engine engine,  byte[] wasm) {
        super(NewFramework.INSTANCE.create_template_new(engine, wasm, wasm.length));
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
        NewFramework.INSTANCE.free_template(this);
    }

    public Instance instantiate(Engine engine, HostFunction[] functions, LinearMemory[] memories, boolean withWasi) {
        Pointer[] functionsPtr = HostFunction.arrayToPointer(functions);
        Pointer[] memoriesPtr = LinearMemory.arrayToPointer(memories);

        return NewFramework.INSTANCE.instantiate(engine, this, functionsPtr, functionsPtr.length, memoriesPtr, memoriesPtr.length, withWasi);
    }
}
