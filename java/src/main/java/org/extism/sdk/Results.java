package org.extism.sdk;

public class Results extends Parameters implements AutoCloseable {

    public Results(int length) {
        super(length);
    }

    public Results(LibExtism.ExtismVal.ByReference ptr, int length) {
        super(ptr, length);
    }
}
