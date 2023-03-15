package org.extism.sdk.parameters;

import org.extism.sdk.LibExtism;

public class Parameters {
    private LibExtism.ExtismVal.ByReference ptr;
    private LibExtism.ExtismVal[] values;

    public Parameters(int length) {
        this.ptr = new LibExtism.ExtismVal.ByReference();
        this.values = (LibExtism.ExtismVal []) this.ptr.toArray(length);
    }

    public Parameters(LibExtism.ExtismVal.ByReference ptr, int length) {
        this.ptr = ptr;
        this.values = (LibExtism.ExtismVal []) this.ptr.toArray(length);
    }

    public LibExtism.ExtismVal[] getValues() {
        return values;
    }

    public LibExtism.ExtismVal getValue(int pos) {
        return values[pos];
    }

    public LibExtism.ExtismVal.ByReference getPtr() {
        return ptr;
    }

    public void set(AddFunction function, int i)  {
        this.values[i] = function.invoke(this.values[i]);
    }

    interface AddFunction {
        LibExtism.ExtismVal invoke(LibExtism.ExtismVal item);
    }
}
