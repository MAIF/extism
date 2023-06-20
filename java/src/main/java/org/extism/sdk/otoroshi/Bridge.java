package org.extism.sdk.otoroshi;

import com.sun.jna.*;

public interface Bridge extends Library {

    Bridge INSTANCE = Native.load("extismotoroshi", Bridge.class);

    interface InternalExtismFunction extends Callback {
        void invoke(
                OtoroshiInternal currentPlugin,
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

    Pointer otoroshi_create_wasmtime_engine();
    Pointer otoroshi_create_template_new(OtoroshiEngine engine, byte[] wasm, int wasmLength);

    OtoroshiInstance otoroshi_instantiate(OtoroshiEngine engine,
                                          OtoroshiTemplate template,
                                          Pointer[] functionsPtr,
                                          int functionsLength,
                                          OtoroshiMemory[] memoriesPtr,
                                          int memoriesLength,
                                          boolean withWasi);

    Bridge.ExtismVal.ByReference otoroshi_call(OtoroshiInstance instance, String functionName, Bridge.ExtismVal.ByReference inputs, int length);
    Pointer otoroshi_wasm_plugin_call_without_params(OtoroshiInstance template, String functionName);

    void otoroshi_wasm_plugin_call_without_results(OtoroshiInstance template,
                                                   String functionName,
                                                   Bridge.ExtismVal.ByReference inputs,
                                                   int nInputs);

    Pointer otoroshi_create_wasmtime_memory(String name, String namespace, int minPages, int maxPages);


    int otoroshi_extism_current_plugin_memory_length(OtoroshiInternal plugin, long n);
    Pointer otoroshi_extism_current_plugin_memory(OtoroshiInternal plugin);
    int otoroshi_extism_current_plugin_memory_alloc(OtoroshiInternal plugin, long n);
    void otoroshi_extism_current_plugin_memory_free(OtoroshiInternal plugin, long ptr);
    Pointer otoroshi_extism_get_lineary_memory_from_host_functions(OtoroshiInternal plugin, int instanceIndex, String memoryName);

    int otoroshi_bridge_extism_plugin_call(OtoroshiInstance instance, String function_name, byte[] data, int dataLength);
    int otoroshi_bridge_extism_plugin_output_length(OtoroshiInstance instance);
    Pointer otoroshi_bridge_extism_plugin_output_data(OtoroshiInstance instance);

    Pointer extism_function_new(String name,
                                int[] inputs,
                                int nInputs,
                                int[] outputs,
                                int nOutputs,
                                Bridge.InternalExtismFunction func,
                                Pointer userData,
                                Pointer freeUserData);

    void extism_function_set_namespace(Pointer p, String name);

    void otoroshi_deallocate_results(Bridge.ExtismVal.ByReference results, int length);

    void otoroshi_free_plugin(OtoroshiInstance instance);
    void otoroshi_free_engine(OtoroshiEngine engine);
    void otoroshi_free_memory(OtoroshiMemory memory);
    void otoroshi_free_template(OtoroshiTemplate template);
    void otoroshi_free_function(Pointer function);
}
