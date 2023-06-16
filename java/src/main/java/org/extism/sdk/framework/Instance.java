package org.extism.sdk.framework;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;

import java.nio.charset.StandardCharsets;

public class Instance extends PointerType implements AutoCloseable {

    public String extismCall(String functionName, byte[] inputData) {
        int inputDataLength = inputData == null ? 0 : inputData.length;
        int exitCode = NewFramework.INSTANCE.extism_plugin_call(this, functionName, inputData, inputDataLength);

        if (exitCode == -1) {
//            String error = this.error(this);
//            throw new ExtismException(error);
        }

        int length = NewFramework.INSTANCE.extism_plugin_output_length(this);
        Pointer output = NewFramework.INSTANCE.extism_plugin_output_data(this);
        return new String(output.getByteArray(0, length), StandardCharsets.UTF_8);
    }

    public Results call(String functionName, Parameters params, int resultsLength) {
        params.getPtr().write();

        NewFramework.ExtismVal.ByReference results = NewFramework.INSTANCE.call(
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
        Pointer results = NewFramework.INSTANCE.wasm_plugin_call_without_params(
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

        NewFramework.INSTANCE.wasm_plugin_call_without_results(
                this,
                functionName,
                params.getPtr(),
                params.getLength());
    }

    public void freeResults(Results results) {
        NewFramework.INSTANCE.deallocate_results(results.getPtr(), results.getLength());
    }

    public void free() {
        NewFramework.INSTANCE.free_plugin(this);
    }

    @Override
    public void close() throws Exception {
        free();
    }
}
