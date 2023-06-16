package org.extism.sdk.framework;

public class Results extends Parameters implements AutoCloseable {

    public Results(int length) {
        super(length);
    }

    public Results(NewFramework.ExtismVal.ByReference ptr, int length) {
        super(ptr, length);
    }
}
