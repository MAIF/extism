package org.extism.sdk.parameters;


import org.extism.sdk.framework.NewFramework;

public class Parameters implements AutoCloseable {
    protected NewFramework.ExtismVal.ByReference ptr;
    protected NewFramework.ExtismVal[] values;
    private final int length;

    public Parameters(int length) {
        this.ptr = new NewFramework.ExtismVal.ByReference();

        if (length > 0) {
            this.values = (NewFramework.ExtismVal[]) this.ptr.toArray(length);
        }

        this.length = length;
    }

    public Parameters(NewFramework.ExtismVal.ByReference ptr, int length) {
        this.ptr = ptr;
        this.length = length;

        if (length > 0) {
            this.values = (NewFramework.ExtismVal []) this.ptr.toArray(length);
        }
    }

    public NewFramework.ExtismVal[] getValues() {
        return values;
    }

    public NewFramework.ExtismVal getValue(int pos) {
        return values[pos];
    }

    public NewFramework.ExtismVal.ByReference getPtr() {
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
        NewFramework.INSTANCE.deallocate_results(this.ptr, this.length);
    }

    interface AddFunction {
        NewFramework.ExtismVal invoke(NewFramework.ExtismVal item);
    }
}
