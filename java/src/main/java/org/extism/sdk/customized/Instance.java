package org.extism.sdk.customized;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;

import java.nio.charset.StandardCharsets;

public class Instance extends PointerType implements AutoCloseable {

    public String extismCall(String functionName, byte[] inputData) {
        int inputDataLength = inputData == null ? 0 : inputData.length;
        int exitCode = Bridge.INSTANCE.extism_plugin_call(this, functionName, inputData, inputDataLength);

        if (exitCode == -1) {
//            String error = this.error(this);
//            throw new ExtismException(error);
        }

        int length = Bridge.INSTANCE.extism_plugin_output_length(this);
        Pointer output = Bridge.INSTANCE.extism_plugin_output_data(this);
        return new String(output.getByteArray(0, length), StandardCharsets.UTF_8);
    }

    public Results call(String functionName, Parameters params, int resultsLength) {
        params.getPtr().write();

        Bridge.ExtismVal.ByReference results = Bridge.INSTANCE.call(
                this,
                functionName,
                params.getPtr(),
                params.getLength());

        if (results == null) {
            return new Results(0);
        } else {
            return new Results(results, resultsLength);
        }
    }

    public Pointer callWithoutParams(String functionName, int resultsLength) {
        Pointer results = Bridge.INSTANCE.wasm_plugin_call_without_params(
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

    public void callWithoutResults(String functionName, Parameters params) {
        params.getPtr().write();

        Bridge.INSTANCE.wasm_plugin_call_without_results(
                this,
                functionName,
                params.getPtr(),
                params.getLength());
    }

    public void freeResults(Results results) {
        Bridge.INSTANCE.deallocate_results(results.getPtr(), results.getLength());
    }

    public void free() {
        Bridge.INSTANCE.free_plugin(this);
    }

    @Override
    public void close() throws Exception {
        free();
    }
}
