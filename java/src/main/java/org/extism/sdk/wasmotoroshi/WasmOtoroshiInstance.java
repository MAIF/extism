package org.extism.sdk.wasmotoroshi;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class WasmOtoroshiInstance extends PointerType implements AutoCloseable {

    private AtomicBoolean closed = new AtomicBoolean(false);

    public String extismCall(String functionName, byte[] inputData) {
        if(!closed.get()) {
            int inputDataLength = inputData == null ? 0 : inputData.length;
            int exitCode = WasmBridge.INSTANCE.wasm_otoroshi_bridge_extism_plugin_call(this, functionName, inputData, inputDataLength);

            if (exitCode == -1) {
                return String.valueOf(exitCode);
            }

            int length = WasmBridge.INSTANCE.wasm_otoroshi_bridge_extism_plugin_output_length(this);
            Pointer output = WasmBridge.INSTANCE.wasm_otoroshi_bridge_extism_plugin_output_data(this);
            return new String(output.getByteArray(0, length), StandardCharsets.UTF_8);
        }
        return "";
    }

    public WasmOtoroshiResults call(String functionName, WasmOtoroshiParameters params, int resultsLength) {
        if(!closed.get()) {
            params.getPtr().write();

            WasmBridge.ExtismVal.ByReference results = WasmBridge.INSTANCE.wasm_otoroshi_call(
                    this,
                    functionName,
                    params.getPtr(),
                    params.getLength());

            if (results == null) {
                return new WasmOtoroshiResults(0);
            } else {
                return new WasmOtoroshiResults(results, resultsLength);
            }
        }
        return null;
    }

    public Pointer getCustomData() {
        return WasmBridge.INSTANCE.get_custom_data(this.getPointer());
    }

    public Pointer callWithoutParams(String functionName, int resultsLength) {
        if(!closed.get()) {
            Pointer results = WasmBridge.INSTANCE.wasm_otoroshi_wasm_plugin_call_without_params(
                    this,
                    functionName);


            if (results == null) {
                if (resultsLength > 0) {
                    return null;
                } else {
                    return null;
                }
            } else {
                return results;
            }
        }
        return null;
    }

    public void callWithoutResults(String functionName, WasmOtoroshiParameters params) {
        if(!closed.get()) {
            params.getPtr().write();

            WasmBridge.INSTANCE.wasm_otoroshi_wasm_plugin_call_without_results(
                    this,
                    functionName,
                    params.getPtr(),
                    params.getLength());
        }
    }

    public void freeResults(WasmOtoroshiResults results) {
        WasmBridge.INSTANCE.wasm_otoroshi_deallocate_results(results.getPtr(), results.getLength());
    }

    public int writeBytes(byte[] data, int n, int offset) {
        if(!closed.get()) {
            return WasmBridge.INSTANCE.wasm_otoroshi_extism_memory_write_bytes(this, data, n, offset);
        }
        return -1;
    }

    public Pointer getMemory(String name) {
        return WasmBridge.INSTANCE.wasm_otoroshi_extism_get_memory(this.getPointer(), name);
    }

    public void reset() {
        WasmBridge.INSTANCE.wasm_otoroshi_extism_reset(this);
    }

    public String getError() {
        return WasmBridge.INSTANCE.wasm_otoroshi_instance_error(this);
    }

    public void free() {
        if(!closed.compareAndSet(false, true)) {
            WasmBridge.INSTANCE.wasm_otoroshi_free_plugin(this);
        }
    }

    public int getMemorySize() {
        return WasmBridge.INSTANCE.wasm_otoroshi_extism_memory_bytes(this);
    }

    @Override
    public void close() throws Exception {
        if(!closed.compareAndSet(false, true)) {
            free();
        }
    }
}
