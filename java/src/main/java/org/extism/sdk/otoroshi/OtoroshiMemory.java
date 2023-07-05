package org.extism.sdk.otoroshi;

import com.sun.jna.PointerType;

public class OtoroshiMemory extends PointerType implements AutoCloseable {

    private String name;
    private String namespace;
    private int minPages;
    private int maxPages;

    public OtoroshiMemory() {}

    public OtoroshiMemory(String name, String namespace, int minPages, int maxPages) {
        super(Bridge.INSTANCE.otoroshi_create_wasmtime_memory(
                name,
                namespace,
                minPages,
                maxPages));

        this.name = name;
        this.namespace = namespace;
        this.minPages = minPages;
        this.maxPages = maxPages;
    }

    @Override
    public void close() {
        Bridge.INSTANCE.otoroshi_free_memory(this);
    }
}
