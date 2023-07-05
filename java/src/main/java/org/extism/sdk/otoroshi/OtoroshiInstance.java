package org.extism.sdk.otoroshi;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import org.extism.sdk.LibExtism;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class OtoroshiInstance extends PointerType implements AutoCloseable {

    private AtomicBoolean closed = new AtomicBoolean(false);

    public String extismCall(String functionName, byte[] inputData) {
        if(!closed.get()) {
            int inputDataLength = inputData == null ? 0 : inputData.length;
            int exitCode = Bridge.INSTANCE.otoroshi_bridge_extism_plugin_call(this, functionName, inputData, inputDataLength);

            if (exitCode == -1) {
                return String.valueOf(exitCode);
            }

            int length = Bridge.INSTANCE.otoroshi_bridge_extism_plugin_output_length(this);
            Pointer output = Bridge.INSTANCE.otoroshi_bridge_extism_plugin_output_data(this);
            return new String(output.getByteArray(0, length), StandardCharsets.UTF_8);
        }
        return "";
    }

    public OtoroshiResults call(String functionName, OtoroshiParameters params, int resultsLength) {
        if(!closed.get()) {
            params.getPtr().write();

            Bridge.ExtismVal.ByReference results = Bridge.INSTANCE.otoroshi_call(
                    this,
                    functionName,
                    params.getPtr(),
                    params.getLength());

            if (results == null) {
                return new OtoroshiResults(0);
            } else {
                return new OtoroshiResults(results, resultsLength);
            }
        }
        return null;
    }

    public Pointer callWithoutParams(String functionName, int resultsLength) {
        if(!closed.get()) {
            Pointer results = Bridge.INSTANCE.otoroshi_wasm_plugin_call_without_params(
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

    public void callWithoutResults(String functionName, OtoroshiParameters params) {
        if(!closed.get()) {
            params.getPtr().write();

            Bridge.INSTANCE.otoroshi_wasm_plugin_call_without_results(
                    this,
                    functionName,
                    params.getPtr(),
                    params.getLength());
        }
    }

    public void freeResults(OtoroshiResults results) {
        Bridge.INSTANCE.otoroshi_deallocate_results(results.getPtr(), results.getLength());
    }

    public int writeBytes(byte[] data, int n, int offset) {
        if(!closed.get()) {
            return Bridge.INSTANCE.otoroshi_extism_memory_write_bytes(this, data, n, offset);
        }
        return -1;
    }

    public Pointer getMemory(String name) {
        return Bridge.INSTANCE.otoroshi_extism_get_memory(this.getPointer(), name);
    }

    public void reset() {
        Bridge.INSTANCE.otoroshi_extism_reset(this);
    }

    public String getError() {
        return Bridge.INSTANCE.otoroshi_instance_error(this);
    }

    public void free() {
        if(!closed.compareAndSet(false, true)) {
            Bridge.INSTANCE.otoroshi_free_plugin(this);
        }
    }

    @Override
    public void close() throws Exception {
        if(!closed.compareAndSet(false, true)) {
            free();
        }
    }
}
