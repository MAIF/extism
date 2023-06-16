package org.extism.sdk;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import org.extism.sdk.framework.NewFramework;

import java.util.Arrays;
import java.util.Optional;

public class HostFunction<T extends HostUserData> {

    private final NewFramework.InternalExtismFunction callback;

    public final Pointer pointer;

    public final String name;

    public final NewFramework.ExtismValType[] params;

    public final NewFramework.ExtismValType[] returns;

    public final Optional<T> userData;

    public HostFunction(String name, NewFramework.ExtismValType[] params, NewFramework.ExtismValType[] returns, ExtismFunction f, Optional<T> userData) {

        this.name = name;
        this.params = params;
        this.returns = returns;
        this.userData = userData;
        this.callback = (Pointer currentPlugin,
                         NewFramework.ExtismVal inputs,
                         int nInputs,
                         NewFramework.ExtismVal outs,
                         int nOutputs,
                         Pointer data) -> {

            NewFramework.ExtismVal[] outputs = (NewFramework.ExtismVal []) outs.toArray(nOutputs);

            f.invoke(
                    new ExtismCurrentPlugin(currentPlugin),
                    (NewFramework.ExtismVal []) inputs.toArray(nInputs),
                    outputs,
                    userData
            );

            for (NewFramework.ExtismVal output : outputs) {
                convertOutput(output, output);
            }
        };

        this.pointer = NewFramework.INSTANCE.extism_function_new(
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

    void convertOutput(NewFramework.ExtismVal original, NewFramework.ExtismVal fromHostFunction) {
        if (fromHostFunction.t != original.t)
            throw new ExtismException(String.format("Output type mismatch, got %d but expected %d", fromHostFunction.t, original.t));

        if (fromHostFunction.t == NewFramework.ExtismValType.I32.v) {
            original.v.setType(Integer.TYPE);
            original.v.i32 = fromHostFunction.v.i32;
        } else if (fromHostFunction.t == NewFramework.ExtismValType.I64.v) {
            original.v.setType(Long.TYPE);
            original.v.i64 = fromHostFunction.v.i64;
        } else if (fromHostFunction.t == NewFramework.ExtismValType.F32.v) {
            original.v.setType(Float.TYPE);
            original.v.f32 = fromHostFunction.v.f32;
        } else if (fromHostFunction.t == NewFramework.ExtismValType.F64.v) {
            original.v.setType(Double.TYPE);
            original.v.f64 = fromHostFunction.v.f64;
        } else
            throw new ExtismException(String.format("Unsupported return type: %s", original.t));
    }


    public static Pointer[] arrayToPointer(HostFunction[] functions) {
        Pointer[] ptrArr = new Pointer[functions == null ? 0 : functions.length];

        if (functions != null)
            for (int i = 0; i < functions.length; i++) {
                ptrArr[i] = functions[i].pointer;
            }

        return ptrArr;
    }

    public void setNamespace(String name) {
        if (this.pointer != null) {
            NewFramework.INSTANCE.extism_function_set_namespace(this.pointer, name);
        }
    }

    public HostFunction withNamespace(String name) {
        this.setNamespace(name);
        return this;
    }
}
