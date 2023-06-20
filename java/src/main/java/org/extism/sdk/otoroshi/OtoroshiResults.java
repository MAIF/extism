package org.extism.sdk.otoroshi;

public class OtoroshiResults extends OtoroshiParameters implements AutoCloseable {

    public OtoroshiResults(int length) {
        super(length);
    }

    public OtoroshiResults(Bridge.ExtismVal.ByReference ptr, int length) {
        super(ptr, length);
    }
}
