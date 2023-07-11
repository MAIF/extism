package org.extism.sdk.wasmotoroshi;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;

import java.util.Arrays;
import java.util.Optional;

public class WasmOtoroshiHostFunction<T extends WasmOtoroshiHostUserData> implements AutoCloseable {

    private final WasmBridge.InternalExtismFunction callback;

    public final Pointer pointer;

    public final String name;

    public final WasmBridge.ExtismValType[] params;

    public final WasmBridge.ExtismValType[] returns;

    public final Optional<T> userData;

    public WasmOtoroshiHostFunction(String name, WasmBridge.ExtismValType[] params, WasmBridge.ExtismValType[] returns, WasmOtoroshiExtismFunction f, Optional<T> userData) {

        this.name = name;
        this.params = params;
        this.returns = returns;
        this.userData = userData;
        this.callback = (WasmOtoroshiInternal content,
                         WasmBridge.ExtismVal inputs,
                         int nInputs,
                         WasmBridge.ExtismVal outs,
                         int nOutputs,
                         Pointer data) -> {

            WasmBridge.ExtismVal[] outputs = (WasmBridge.ExtismVal []) outs.toArray(nOutputs);

            f.invoke(
                    content,
                    (WasmBridge.ExtismVal []) inputs.toArray(nInputs),
                    outputs,
                    userData
            );

            for (WasmBridge.ExtismVal output : outputs) {
                convertOutput(output, output);
            }
        };

        this.pointer = WasmBridge.INSTANCE.wasm_otoroshi_extism_function_new(
                this.name,
                Arrays.stream(this.params).mapToInt(r -> r.v).toArray(),
                this.params.length,
                Arrays.stream(this.returns).mapToInt(r -> r.v).toArray(),
                this.returns.length,
                this.callback,
                userData.map(PointerType::getPointer).orElse(null),
                null
        );
    }

    void convertOutput(WasmBridge.ExtismVal original, WasmBridge.ExtismVal fromHostFunction) throws Exception {
        if (fromHostFunction.t != original.t)
            throw new Exception(String.format("Output type mismatch, got %d but expected %d", fromHostFunction.t, original.t));

        if (fromHostFunction.t == WasmBridge.ExtismValType.I32.v) {
            original.v.setType(Integer.TYPE);
            original.v.i32 = fromHostFunction.v.i32;
        } else if (fromHostFunction.t == WasmBridge.ExtismValType.I64.v) {
            original.v.setType(Long.TYPE);
            original.v.i64 = fromHostFunction.v.i64;
        } else if (fromHostFunction.t == WasmBridge.ExtismValType.F32.v) {
            original.v.setType(Float.TYPE);
            original.v.f32 = fromHostFunction.v.f32;
        } else if (fromHostFunction.t == WasmBridge.ExtismValType.F64.v) {
            original.v.setType(Double.TYPE);
            original.v.f64 = fromHostFunction.v.f64;
        } else
            throw new Exception(String.format("Unsupported return type: %s", original.t));
    }


    public static Pointer[] arrayToPointer(org.extism.sdk.wasmotoroshi.WasmOtoroshiHostFunction[] functions) {
        Pointer[] ptrArr = new Pointer[functions == null ? 0 : functions.length];

        if (functions != null)
            for (int i = 0; i < functions.length; i++) {
                ptrArr[i] = functions[i].pointer;
            }

        return ptrArr;
    }

    public void setNamespace(String name) {
        if (this.pointer != null) {
            WasmBridge.INSTANCE.extism_function_set_namespace(this.pointer, name);
        }
    }

    public org.extism.sdk.wasmotoroshi.WasmOtoroshiHostFunction withNamespace(String name) {
        this.setNamespace(name);
        return this;
    }

    @Override
    public void close() throws Exception {
        WasmBridge.INSTANCE.wasm_otoroshi_free_function(this.pointer);
    }
}
