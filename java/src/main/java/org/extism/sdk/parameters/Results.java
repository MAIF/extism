package org.extism.sdk.parameters;

import org.extism.sdk.LibExtism;

public class Results extends Parameters {

    public Results(int length) {
        super(length);
    }

    public Results(LibExtism.ExtismVal.ByReference ptr, int length) {
        super(ptr, length);
    }
}
