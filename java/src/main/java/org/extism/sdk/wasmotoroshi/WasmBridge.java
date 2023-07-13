package org.extism.sdk.wasmotoroshi;

import com.sun.jna.*;

public interface WasmBridge extends Library {

    WasmBridge INSTANCE = Native.load("extism", WasmBridge.class);

    interface InternalExtismFunction extends Callback {
        void invoke(
                WasmOtoroshiInternal currentPlugin,
                ExtismVal inputs,
                int nInputs,
                ExtismVal outputs,
                int nOutputs,
                Pointer data
        ) throws Exception;
    }

    @Structure.FieldOrder({"t", "v"})
    class ExtismVal extends Structure {
        public static class ByReference extends ExtismVal implements Structure.ByReference {
            public ByReference(Pointer ptr) {
                super(ptr);
            }

            public ByReference() {}
        }

        public ExtismVal() {}

        public ExtismVal(Pointer p) {
            super(p);
        }

        public int t;
        public ExtismValUnion v;

        @Override
        public String toString() {
            String typeAsString = new String[]{"int", "long", "float", "double"}[t];

            Object unionValue = new Object[]{v.i32, v.i64, v.f32, v.f64}[t];

            return String.format("ExtismVal(%s, %s)", typeAsString, unionValue);
        }
    }

    class ExtismValUnion extends Union {
        public int i32;
        public long i64;
        public float f32;
        public double f64;
    }

    enum ExtismValType {
        I32(0),
        I64(1),
        F32(2),
        F64(3),
        V128(4),
        FuncRef(5),
        ExternRef(6);

        public final int v;

        ExtismValType(int value) {
            this.v = value;
        }
    }

    Pointer wasm_otoroshi_create_wasmtime_engine();
    Pointer wasm_otoroshi_create_template_new(WasmOtoroshiEngine engine, byte[] wasm, int wasmLength);

    WasmOtoroshiInstance wasm_otoroshi_instantiate(WasmOtoroshiEngine engine,
                                          WasmOtoroshiTemplate template,
                                          Pointer[] functionsPtr,
                                          int functionsLength,
                                          WasmOtoroshiMemory[] memoriesPtr,
                                          int memoriesLength,
                                          boolean withWasi);

    org.extism.sdk.wasmotoroshi.WasmBridge.ExtismVal.ByReference wasm_otoroshi_call(WasmOtoroshiInstance instance, String functionName, org.extism.sdk.wasmotoroshi.WasmBridge.ExtismVal.ByReference inputs, int length);
    Pointer wasm_otoroshi_wasm_plugin_call_without_params(WasmOtoroshiInstance template, String functionName);

    void wasm_otoroshi_wasm_plugin_call_without_results(WasmOtoroshiInstance template,
                                                   String functionName,
                                                   org.extism.sdk.wasmotoroshi.WasmBridge.ExtismVal.ByReference inputs,
                                                   int nInputs);

    Pointer wasm_otoroshi_create_wasmtime_memory(String name, String namespace, int minPages, int maxPages);

    int wasm_otoroshi_extism_current_plugin_memory_length(WasmOtoroshiInternal plugin, long n);
    Pointer wasm_otoroshi_extism_current_plugin_memory(WasmOtoroshiInternal plugin);
    int wasm_otoroshi_extism_current_plugin_memory_alloc(WasmOtoroshiInternal plugin, long n);
    void wasm_otoroshi_extism_current_plugin_memory_free(WasmOtoroshiInternal plugin, long ptr);

    int wasm_otoroshi_bridge_extism_plugin_call(WasmOtoroshiInstance instance, String function_name, byte[] data, int dataLength);
    Pointer get_custom_data(Pointer instance);

    int wasm_otoroshi_bridge_extism_plugin_output_length(WasmOtoroshiInstance instance);
    Pointer wasm_otoroshi_bridge_extism_plugin_output_data(WasmOtoroshiInstance instance);
    void wasm_otoroshi_extism_reset(WasmOtoroshiInstance instance);

    int wasm_otoroshi_extism_memory_write_bytes(WasmOtoroshiInstance instance, byte[] data, int n, int offset);

    Pointer wasm_otoroshi_extism_get_memory(Pointer instance, String memoryName);

    Pointer wasm_otoroshi_extism_function_new(String name,
                                int[] inputs,
                                int nInputs,
                                int[] outputs,
                                int nOutputs,
                                WasmBridge.InternalExtismFunction func,
                                Pointer userData,
                                Pointer freeUserData);

    void extism_function_set_namespace(Pointer p, String name);

    String wasm_otoroshi_instance_error(WasmOtoroshiInstance instance);

    void wasm_otoroshi_deallocate_results(WasmBridge.ExtismVal.ByReference results, int length);

    int wasm_otoroshi_extism_memory_bytes(WasmOtoroshiInstance instance);

    void wasm_otoroshi_free_plugin(WasmOtoroshiInstance instance);
    void wasm_otoroshi_free_engine(WasmOtoroshiEngine engine);
    void wasm_otoroshi_free_memory(WasmOtoroshiMemory memory);
    void wasm_otoroshi_free_template(WasmOtoroshiTemplate template);
    void wasm_otoroshi_free_function(Pointer function);
}
