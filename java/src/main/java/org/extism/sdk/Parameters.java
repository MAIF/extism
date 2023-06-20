package org.extism.sdk;

public class Parameters implements AutoCloseable {
    protected LibExtism.ExtismVal.ByReference ptr;
    protected LibExtism.ExtismVal[] values;
    private final int length;

    private int next = 0;

    public Parameters(int length) {
        this.ptr = new LibExtism.ExtismVal.ByReference();

        if (length > 0) {
            this.values = (LibExtism.ExtismVal[]) this.ptr.toArray(length);
        }

        this.next = 0;
        this.length = length;
    }

    public Parameters(LibExtism.ExtismVal.ByReference ptr, int length) {
        this.ptr = ptr;
        this.length = length;

        if (length > 0) {
            this.values = (LibExtism.ExtismVal []) this.ptr.toArray(length);
        }
    }

    public Parameters pushInt(int value) {
        int length = getNext();
        this.values[length].t = 0;
        this.values[length].v.setType(java.lang.Integer.TYPE);
        this.values[length].v.i32 = value;

        return this;
    }

    public Parameters pushInts(int ...values) {
        for (int value : values) {
            int length = getNext();
            this.values[length].t = 0;
            this.values[length].v.setType(Integer.TYPE);
            this.values[length].v.i32 = value;
        }

        return this;
    }

    public Parameters pushLong(long value) {
        int length = getNext();
        this.values[length].t = 0;
        this.values[length].v.setType(java.lang.Long.TYPE);
        this.values[length].v.i64 = value;
        return this;
    }

    public Parameters pushLongs(long ...values) {
        for (long value : values) {
            int length = getNext();
            this.values[length].t = 0;
            this.values[length].v.setType(Long.TYPE);
            this.values[length].v.i64 = value;
        }
        return this;
    }

    public Parameters pushFloat(float value) {
        int length = getNext();
        this.values[length].t = 0;
        this.values[length].v.setType(java.lang.Float.TYPE);
        this.values[length].v.f32 = value;
        return this;
    }

    public Parameters pushFloats(float ...values) {
        for (float value : values) {
            int length = getNext();
            this.values[length].t = 0;
            this.values[length].v.setType(Float.TYPE);
            this.values[length].v.f32 = value;
        }
        return this;
    }

    public Parameters pushDouble(double value) {
        int length = getNext();
        this.values[length].t = 0;
        this.values[length].v.setType(java.lang.Double.TYPE);
        this.values[length].v.f64 = value;
        return this;
    }

    public Parameters pushDouble(double ...values) {
        for (double value : values) {
            int length = getNext();
            this.values[length].t = 0;
            this.values[length].v.setType(Double.TYPE);
            this.values[length].v.f64 = value;
        }
        return this;
    }

    private int getNext() {
        int result = next;
        next += 1;
        return result;
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

    public void set(Parameters.AddFunction function, int i)  {
        this.values[i] = function.invoke(this.values[i]);
    }

    @Override
    public void close() {
        LibExtism.INSTANCE.deallocate_results(this.ptr, this.length);
    }

    interface AddFunction {
        LibExtism.ExtismVal invoke(LibExtism.ExtismVal item);
    }
}

