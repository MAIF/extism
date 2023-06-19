package org.extism.sdk;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;

public class LinearMemory {

    private final String name;
    private String namespace = "env";
    private LinearMemoryOptions memoryOptions;

    private final Pointer pointer;

    public LinearMemory(String name, LinearMemoryOptions memoryOptions) {
        this.name = name;
        this.memoryOptions = memoryOptions;

        this.pointer = this.instanciate();
    }

    public LinearMemory(String name, String namespace, LinearMemoryOptions memoryOptions) {
        this.name = name;
        this.namespace = namespace;
        this.memoryOptions = memoryOptions;

        this.pointer = this.instanciate();
    }

    private Pointer instanciate() {
        return LibExtism.INSTANCE.extism_memory_new(
                this.name,
                this.namespace,
                this.memoryOptions.getMin(),
                this.memoryOptions.getMax().orElse(0));
    }

    public static Pointer[] arrayToPointer(LinearMemory[] memories) {
        Pointer[] ptr = new Pointer[memories == null ? 0 : memories.length];

        if (memories != null)
            for (int i = 0; i < memories.length; i++) {
                ptr[i] = memories[i].pointer;
            }

        return ptr;
    }

    public String getName() {
        return name;
    }

    public LinearMemoryOptions getMemoryOptions() {
        return memoryOptions;
    }

    public String getNamespace() {
        return namespace;
    }

    public Pointer getPointer() {
        return pointer;
    }
}
