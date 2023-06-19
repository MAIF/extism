package org.extism.sdk;

import org.extism.sdk.customized.Memory;

public class LinearMemory implements AutoCloseable {

    private final String name;
    private String namespace = "env";
    private LinearMemoryOptions memoryOptions;

    private final Memory memory;

    public LinearMemory(String name, LinearMemoryOptions memoryOptions) {
        this.name = name;
        this.memoryOptions = memoryOptions;

        this.memory = this.instanciate();
    }

    public LinearMemory(String name, String namespace, LinearMemoryOptions memoryOptions) {
        this.name = name;
        this.namespace = namespace;
        this.memoryOptions = memoryOptions;

        this.memory = this.instanciate();
    }

    private Memory instanciate() {
        return new Memory(
                this.name,
                this.namespace,
                this.memoryOptions.getMin(),
                this.memoryOptions.getMax().orElse(0));
    }

    public static Memory[] arrayToPointer(LinearMemory[] memories) {
        Memory[] ptr = new Memory[memories == null ? 0 : memories.length];

        if (memories != null)
            for (int i = 0; i < memories.length; i++) {
                ptr[i] = memories[i].memory;
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

    public Memory getMemory() {
        return memory;
    }

    @Override
    public void close() throws Exception {

    }
}
