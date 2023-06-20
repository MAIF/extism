package org.extism.sdk.otoroshi;

public class OtoroshiLinearMemory {

    private final String name;
    private String namespace = "env";
    private OtoroshiLinearMemoryOptions memoryOptions;

    private final OtoroshiMemory memory;

    public OtoroshiLinearMemory(String name, OtoroshiLinearMemoryOptions memoryOptions) {
        this.name = name;
        this.memoryOptions = memoryOptions;

        this.memory = this.instanciate();
    }

    public OtoroshiLinearMemory(String name, String namespace, OtoroshiLinearMemoryOptions memoryOptions) {
        this.name = name;
        this.namespace = namespace;
        this.memoryOptions = memoryOptions;

        this.memory = this.instanciate();
    }

    private OtoroshiMemory instanciate() {
        return new OtoroshiMemory(
                this.name,
                this.namespace,
                this.memoryOptions.getMin(),
                this.memoryOptions.getMax().orElse(0));
    }

    public static OtoroshiMemory[] arrayToPointer(OtoroshiLinearMemory[] memories) {
        OtoroshiMemory[] ptr = new OtoroshiMemory[memories == null ? 0 : memories.length];

        if (memories != null)
            for (int i = 0; i < memories.length; i++) {
                ptr[i] = memories[i].memory;
            }

        return ptr;
    }

    public String getName() {
        return name;
    }

    public OtoroshiLinearMemoryOptions getMemoryOptions() {
        return memoryOptions;
    }

    public String getNamespace() {
        return namespace;
    }

    public OtoroshiMemory getMemory() {
        return memory;
    }

}
