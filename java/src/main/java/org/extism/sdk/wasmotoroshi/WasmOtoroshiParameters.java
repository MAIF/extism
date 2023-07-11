package org.extism.sdk.wasmotoroshi;

public class WasmOtoroshiParameters implements AutoCloseable {
    protected WasmBridge.ExtismVal.ByReference ptr;
    protected WasmBridge.ExtismVal[] values;
    private final int length;

    private int next = 0;

    public WasmOtoroshiParameters(int length) {
        this.ptr = new WasmBridge.ExtismVal.ByReference();

        if (length > 0) {
            this.values = (WasmBridge.ExtismVal[]) this.ptr.toArray(length);
        }

        this.next = 0;
        this.length = length;
    }

    public WasmOtoroshiParameters(WasmBridge.ExtismVal.ByReference ptr, int length) {
        this.ptr = ptr;
        this.length = length;

        if (length > 0) {
            this.values = (WasmBridge.ExtismVal []) this.ptr.toArray(length);
        }
    }

    public org.extism.sdk.wasmotoroshi.WasmOtoroshiParameters pushInt(int value) {
        int length = getNext();
        this.values[length].t = 0;
        this.values[length].v.setType(java.lang.Integer.TYPE);
        this.values[length].v.i32 = value;

        return this;
    }

    public org.extism.sdk.wasmotoroshi.WasmOtoroshiParameters pushInts(int ...values) {
        for (int value : values) {
            int length = getNext();
            this.values[length].t = 0;
            this.values[length].v.setType(Integer.TYPE);
            this.values[length].v.i32 = value;
        }

        return this;
    }

    public org.extism.sdk.wasmotoroshi.WasmOtoroshiParameters pushLong(long value) {
        int length = getNext();
        this.values[length].t = 0;
        this.values[length].v.setType(java.lang.Long.TYPE);
        this.values[length].v.i64 = value;
        return this;
    }

    public org.extism.sdk.wasmotoroshi.WasmOtoroshiParameters pushLongs(long ...values) {
        for (long value : values) {
            int length = getNext();
            this.values[length].t = 0;
            this.values[length].v.setType(Long.TYPE);
            this.values[length].v.i64 = value;
        }
        return this;
    }

    public org.extism.sdk.wasmotoroshi.WasmOtoroshiParameters pushFloat(float value) {
        int length = getNext();
        this.values[length].t = 0;
        this.values[length].v.setType(java.lang.Float.TYPE);
        this.values[length].v.f32 = value;
        return this;
    }

    public org.extism.sdk.wasmotoroshi.WasmOtoroshiParameters pushFloats(float ...values) {
        for (float value : values) {
            int length = getNext();
            this.values[length].t = 0;
            this.values[length].v.setType(Float.TYPE);
            this.values[length].v.f32 = value;
        }
        return this;
    }

    public org.extism.sdk.wasmotoroshi.WasmOtoroshiParameters pushDouble(double value) {
        int length = getNext();
        this.values[length].t = 0;
        this.values[length].v.setType(java.lang.Double.TYPE);
        this.values[length].v.f64 = value;
        return this;
    }

    public org.extism.sdk.wasmotoroshi.WasmOtoroshiParameters pushDouble(double ...values) {
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

    public WasmBridge.ExtismVal[] getValues() {
        return values;
    }

    public WasmBridge.ExtismVal getValue(int pos) {
        return values[pos];
    }

    public WasmBridge.ExtismVal.ByReference getPtr() {
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
        WasmBridge.INSTANCE.wasm_otoroshi_deallocate_results(this.ptr, this.length);
    }

    interface AddFunction {
        WasmBridge.ExtismVal invoke(WasmBridge.ExtismVal item);
    }
}
