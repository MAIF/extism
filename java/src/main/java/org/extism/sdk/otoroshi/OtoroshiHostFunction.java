package org.extism.sdk.otoroshi;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;

import java.util.Arrays;
import java.util.Optional;

public class OtoroshiHostFunction<T extends OtoroshiHostUserData> implements AutoCloseable {

    private final Bridge.InternalExtismFunction callback;

    public final Pointer pointer;

    public final String name;

    public final Bridge.ExtismValType[] params;

    public final Bridge.ExtismValType[] returns;

    public final Optional<T> userData;

    public OtoroshiHostFunction(String name, Bridge.ExtismValType[] params, Bridge.ExtismValType[] returns, OtoroshiExtismFunction f, Optional<T> userData) {

        this.name = name;
        this.params = params;
        this.returns = returns;
        this.userData = userData;
        this.callback = (OtoroshiInternal content,
                         Bridge.ExtismVal inputs,
                         int nInputs,
                         Bridge.ExtismVal outs,
                         int nOutputs,
                         Pointer data) -> {

            Bridge.ExtismVal[] outputs = (Bridge.ExtismVal []) outs.toArray(nOutputs);

            f.invoke(
                    content,
                    (Bridge.ExtismVal []) inputs.toArray(nInputs),
                    outputs,
                    userData
            );

            for (Bridge.ExtismVal output : outputs) {
                convertOutput(output, output);
            }
        };

        this.pointer = Bridge.INSTANCE.otoroshi_extism_function_new(
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

    void convertOutput(Bridge.ExtismVal original, Bridge.ExtismVal fromHostFunction) throws Exception {
        if (fromHostFunction.t != original.t)
            throw new Exception(String.format("Output type mismatch, got %d but expected %d", fromHostFunction.t, original.t));

        if (fromHostFunction.t == Bridge.ExtismValType.I32.v) {
            original.v.setType(Integer.TYPE);
            original.v.i32 = fromHostFunction.v.i32;
        } else if (fromHostFunction.t == Bridge.ExtismValType.I64.v) {
            original.v.setType(Long.TYPE);
            original.v.i64 = fromHostFunction.v.i64;
        } else if (fromHostFunction.t == Bridge.ExtismValType.F32.v) {
            original.v.setType(Float.TYPE);
            original.v.f32 = fromHostFunction.v.f32;
        } else if (fromHostFunction.t == Bridge.ExtismValType.F64.v) {
            original.v.setType(Double.TYPE);
            original.v.f64 = fromHostFunction.v.f64;
        } else
            throw new Exception(String.format("Unsupported return type: %s", original.t));
    }


    public static Pointer[] arrayToPointer(OtoroshiHostFunction[] functions) {
        Pointer[] ptrArr = new Pointer[functions == null ? 0 : functions.length];

        if (functions != null)
            for (int i = 0; i < functions.length; i++) {
                ptrArr[i] = functions[i].pointer;
            }

        return ptrArr;
    }

    public void setNamespace(String name) {
        if (this.pointer != null) {
            Bridge.INSTANCE.extism_function_set_namespace(this.pointer, name);
        }
    }

    public OtoroshiHostFunction withNamespace(String name) {
        this.setNamespace(name);
        return this;
    }

    @Override
    public void close() throws Exception {
        Bridge.INSTANCE.otoroshi_free_function(this.pointer);
    }
}
