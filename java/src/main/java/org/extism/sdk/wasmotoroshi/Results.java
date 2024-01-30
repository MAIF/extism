package org.extism.sdk.wasmotoroshi;

import org.extism.sdk.LibExtism;

public class Results extends Parameters implements AutoCloseable {

    public Results(int length) {
        super(length);
    }

    public Results(LibExtism.ExtismVal.ByReference ptr, int length) {
        super(ptr, length);
    }
}
