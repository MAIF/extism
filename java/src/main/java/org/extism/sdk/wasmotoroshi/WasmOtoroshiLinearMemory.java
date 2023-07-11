package org.extism.sdk.wasmotoroshi;

public class WasmOtoroshiLinearMemory {

    private final String name;
    private String namespace = "env";
    private WasmOtoroshiLinearMemoryOptions memoryOptions;

    private final WasmOtoroshiMemory memory;

    public WasmOtoroshiLinearMemory(String name, WasmOtoroshiLinearMemoryOptions memoryOptions) {
        this.name = name;
        this.memoryOptions = memoryOptions;

        this.memory = this.instanciate();
    }

    public WasmOtoroshiLinearMemory(String name, String namespace, WasmOtoroshiLinearMemoryOptions memoryOptions) {
        this.name = name;
        this.namespace = namespace;
        this.memoryOptions = memoryOptions;

        this.memory = this.instanciate();
    }

    private WasmOtoroshiMemory instanciate() {
        return new WasmOtoroshiMemory(
                this.name,
                this.namespace,
                this.memoryOptions.getMin(),
                this.memoryOptions.getMax().orElse(0));
    }

    public static WasmOtoroshiMemory[] arrayToPointer(org.extism.sdk.wasmotoroshi.WasmOtoroshiLinearMemory[] memories) {
        WasmOtoroshiMemory[] ptr = new WasmOtoroshiMemory[memories == null ? 0 : memories.length];

        if (memories != null)
            for (int i = 0; i < memories.length; i++) {
                ptr[i] = memories[i].memory;
            }

        return ptr;
    }

    public String getName() {
        return name;
    }

    public WasmOtoroshiLinearMemoryOptions getMemoryOptions() {
        return memoryOptions;
    }

    public String getNamespace() {
        return namespace;
    }

    public WasmOtoroshiMemory getMemory() {
        return memory;
    }

}
