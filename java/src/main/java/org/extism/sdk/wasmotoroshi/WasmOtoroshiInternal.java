package org.extism.sdk.wasmotoroshi;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;

import java.nio.charset.StandardCharsets;

public class WasmOtoroshiInternal extends PointerType {

    public Pointer memory() {
        return WasmBridge.INSTANCE.wasm_otoroshi_extism_current_plugin_memory(this);
    }

    public int alloc(int n) {
        return WasmBridge.INSTANCE.wasm_otoroshi_extism_current_plugin_memory_alloc(this, n);
    }

    public Pointer getLinearMemory(String memoryName) {
        return WasmBridge.INSTANCE.wasm_otoroshi_extism_get_memory(this.getPointer(), memoryName);
    }

    public Pointer getCustomData() {
        return WasmBridge.INSTANCE.get_custom_data(this.getPointer());
    }

    public void free(long offset) {
        WasmBridge.INSTANCE.wasm_otoroshi_extism_current_plugin_memory_free(this, offset);
    }

    public long memoryLength(long offset) {
        return WasmBridge.INSTANCE.wasm_otoroshi_extism_current_plugin_memory_length(this, offset);
    }

    // Return a string from a host function
    public void returnString(WasmBridge.ExtismVal output, String s) {
        returnBytes(output, s.getBytes(StandardCharsets.UTF_8));
    }

    // Return bytes from a host function
    public void returnBytes(WasmBridge.ExtismVal output, byte[] b) {
        int offs = this.alloc(b.length);
        Pointer ptr = this.memory();
        ptr.write(offs, b, 0, b.length);
        output.v.i64 = offs;
    }

    // Return int from a host function
    public void returnInt(WasmBridge.ExtismVal output, int v) {
        output.v.i32 = v;
    }

    // Get bytes from host function parameter
    public byte[] inputBytes(WasmBridge.ExtismVal input) throws Exception {
        switch (input.t) {
            case 0:
                return this.memory()
                        .getByteArray(input.v.i32,
                                WasmBridge.INSTANCE.wasm_otoroshi_extism_current_plugin_memory_length(this, input.v.i32));
            case 1:
                return this.memory()
                        .getByteArray(input.v.i64,
                                WasmBridge.INSTANCE.wasm_otoroshi_extism_current_plugin_memory_length(this, input.v.i64));
            default:
                throw new Exception("inputBytes error: ExtismValType " + WasmBridge.ExtismValType.values()[input.t] + " not implemtented");
        }
    }

    // Get string from host function parameter
    public String inputString(WasmBridge.ExtismVal input) throws Exception {
        return new String(this.inputBytes(input));
    }
}