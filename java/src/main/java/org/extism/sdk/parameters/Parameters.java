package org.extism.sdk.parameters;

import org.extism.sdk.LibExtism;

public class Parameters implements AutoCloseable {
    protected LibExtism.ExtismVal.ByReference ptr;
    protected LibExtism.ExtismVal[] values;
    private final int length;

    public Parameters(int length) {
        this.ptr = new LibExtism.ExtismVal.ByReference();

        if (length > 0) {
            this.values = (LibExtism.ExtismVal[]) this.ptr.toArray(length);
        }

        this.length = length;
    }

    public Parameters(LibExtism.ExtismVal.ByReference ptr, int length) {
        this.ptr = ptr;
        this.length = length;

        if (length > 0) {
            this.values = (LibExtism.ExtismVal []) this.ptr.toArray(length);
        }
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

    public int getLength() {
        return length;
    }

    public void set(AddFunction function, int i)  {
        this.values[i] = function.invoke(this.values[i]);
    }

    @Override
    public void close() {
        LibExtism.INSTANCE.deallocate_plugin_call_results(this.ptr, this.length);
    }

    interface AddFunction {
        LibExtism.ExtismVal invoke(LibExtism.ExtismVal item);
    }
}
