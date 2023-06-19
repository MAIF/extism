package org.extism.sdk.customized;

import com.sun.jna.*;

public interface Bridge extends Library {

    Bridge INSTANCE = Native.load("extismdev", Bridge.class);

    interface InternalExtismFunction extends Callback {
        void invoke(
                Internal currentPlugin,
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

    Pointer create_wasmtime_engine();
    Pointer create_template_new(Engine engine, byte[] wasm, int wasmLength);

    Instance instantiate(Engine engine,
                        Template template,
                        Pointer[] functionsPtr,
                        int functionsLength,
                        Memory[] memoriesPtr,
                        int memoriesLength,
                        boolean withWasi);

    Bridge.ExtismVal.ByReference call(Instance instance, String functionName, Bridge.ExtismVal.ByReference inputs, int length);
    Pointer wasm_plugin_call_without_params(Instance template, String functionName);

    void wasm_plugin_call_without_results(Instance template,
                                          String functionName,
                                          Bridge.ExtismVal.ByReference inputs,
                                          int nInputs);

    Pointer create_wasmtime_memory(String name, String namespace, int minPages, int maxPages);


    int extism_current_plugin_memory_length(Internal plugin, long n);
    Pointer extism_current_plugin_memory(Internal plugin);
    int extism_current_plugin_memory_alloc(Internal plugin, long n);
    void extism_current_plugin_memory_free(Internal plugin, long ptr);
    Pointer extism_get_lineary_memory_from_host_functions(Internal plugin, int instanceIndex, String memoryName);

    int extism_plugin_call(Instance instance, String function_name, byte[] data, int dataLength);
    int extism_plugin_output_length(Instance instance);
    Pointer extism_plugin_output_data(Instance instance);

    Pointer extism_function_new(String name,
                                int[] inputs,
                                int nInputs,
                                int[] outputs,
                                int nOutputs,
                                Bridge.InternalExtismFunction func,
                                Pointer userData,
                                Pointer freeUserData);

    void extism_function_set_namespace(Pointer p, String name);

    void deallocate_results(Bridge.ExtismVal.ByReference results, int length);

    void free_plugin(Instance instance);
    void free_engine(Engine engine);
    void free_memory(Memory memory);
    void free_template(Template template);
    void free_function(Pointer function);
}
