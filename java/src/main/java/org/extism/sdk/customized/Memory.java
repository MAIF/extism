package org.extism.sdk.customized;

import com.sun.jna.PointerType;

public class Memory extends PointerType implements AutoCloseable {

    private final String name;
    private final String namespace;
    private final int minPages;
    private final int maxPages;

    public Memory(String name, String namespace, int minPages, int maxPages) {
        super(Bridge.INSTANCE.create_wasmtime_memory(
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
        Bridge.INSTANCE.free_memory(this);
    }
}
