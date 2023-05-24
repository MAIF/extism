package org.extism.sdk.framework;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import org.extism.sdk.parameters.Parameters;
import org.extism.sdk.parameters.Results;

public class Instance extends PointerType  {
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
}
