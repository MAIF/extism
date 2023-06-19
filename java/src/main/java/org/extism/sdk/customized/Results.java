package org.extism.sdk.customized;

public class Results extends Parameters implements AutoCloseable {

    public Results(int length) {
        super(length);
    }

    public Results(Bridge.ExtismVal.ByReference ptr, int length) {
        super(ptr, length);
    }
}
