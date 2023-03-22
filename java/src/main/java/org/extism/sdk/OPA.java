package org.extism.sdk;

import com.sun.jna.Pointer;
import org.extism.sdk.manifest.Manifest;
import org.extism.sdk.manifest.MemoryOptions;
import org.extism.sdk.parameters.IntegerParameter;
import org.extism.sdk.parameters.Parameters;
import org.extism.sdk.parameters.Results;
import org.extism.sdk.wasm.WasmSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

public class OPA {
    private Plugin plugin;

    public OPA(Context ctx, WasmSource regoWasm) {
        Manifest manifest = new Manifest(Arrays.asList(regoWasm));

        ExtismFunction opaAbortFunction = (plugin, params, returns, data) -> {
            System.out.println("opaAbortFunction");
        };
        ExtismFunction opaPrintlnFunction = (plugin, params, returns, data) -> {
            System.out.println("opaPrintlnFunction");
        };
        ExtismFunction opaBuiltin0Function = (plugin, params, returns, data) -> {
            System.out.println("opaBuiltin0Function");
        };
        ExtismFunction opaBuiltin1Function = (plugin, params, returns, data) -> {
            System.out.println("opaBuiltin1Function");
        };
        ExtismFunction opaBuiltin2Function = (plugin, params, returns, data) -> {
            System.out.println("opaBuiltin2Function");
        };
        ExtismFunction opaBuiltin3Function = (plugin, params, returns, data) -> {
            System.out.println("opaBuiltin3Function");
        };
        ExtismFunction opaBuiltin4Function = (plugin, params, returns, data) -> {
            System.out.println("opaBuiltin4Function");
        };

        var parametersTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};
        var resultsTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};

        HostFunction opa_abort = new HostFunction<>(
                "opa_abort",
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32},
                new LibExtism.ExtismValType[0],
                opaAbortFunction,
                Optional.empty()
        );
        HostFunction opa_println = new HostFunction<>(
                "opa_println",
                parametersTypes,
                resultsTypes,
                opaPrintlnFunction,
                Optional.empty()
        );
        HostFunction opa_builtin0 = new HostFunction<>(
                "opa_builtin0",
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32},
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32},
                opaBuiltin0Function,
                Optional.empty()
        );
        HostFunction opa_builtin1 = new HostFunction<>(
                "opa_builtin1",
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32},
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32},
                opaBuiltin1Function,
                Optional.empty()
        );
        HostFunction opa_builtin2 = new HostFunction<>(
                "opa_builtin2",
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32},
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32},
                opaBuiltin2Function,
                Optional.empty()
        );
        HostFunction opa_builtin3 = new HostFunction<>(
                "opa_builtin3",
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32},
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32},
                opaBuiltin3Function,
                Optional.empty()
        );
        HostFunction opa_builtin4 = new HostFunction<>(
                "opa_builtin4",
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32},
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32},
                opaBuiltin4Function,
                Optional.empty()
        );

        HostFunction[] functions = new HostFunction[]{
                opa_abort,
                opa_println,
                opa_builtin0,
                opa_builtin1,
                opa_builtin2,
                opa_builtin3,
                opa_builtin4,
        };

        LinearMemory[] memories = new LinearMemory[]{
                new LinearMemory("memory", "env", new LinearMemoryOptions(5, Optional.empty()))
        };

        this.plugin = ctx.newPlugin(manifest, true, functions, memories);

//        String builtinsFunctions = dumpJSON();
//        System.out.println("Required builtins fuctions : " + (builtinsFunctions.isEmpty() ? "None" : builtinsFunctions));
//
//        this.plugin.evaluate();
//
//        System.out.println(LibExtism.INSTANCE
//                .opa(this.plugin.getPointer(), this.plugin.getIndex())
//                .getString(0));
//        System.out.println(LibExtism.INSTANCE
//                .opa_eval(this.plugin.getPointer(), this.plugin.getIndex(), "{\"method\": \"GET\"}")
//                .getString(0));
//        tryToEvaluate("{\"method\": \"GET\"}");
    }

    int loadJSON(byte[] value) {
        if (value.length == 0) {
            return 0;
        } else {
            int value_buf_len = value.length;
            Parameters parameters = new Parameters(1);
            IntegerParameter parameter = new IntegerParameter();
            parameter.add(parameters, value_buf_len, 0);

            Results raw_addr = plugin.call("opa_malloc", parameters, 1, "".getBytes());

            if(LibExtism.INSTANCE.extism_memory_write_bytes(
                this.plugin.getPointer(),
                this.plugin.getIndex(),
                value,
                value_buf_len,
                raw_addr.getValue(0).v.i32
            ) == -1) {
                throw new ExtismException("Cant' write in memory");
            };

            parameters = new Parameters(2);
            parameter.add(parameters, raw_addr.getValue(0).v.i32, 0);
            parameter.add(parameters, value_buf_len, 1);
            Results parsed_addr = this.plugin.call(
                    "opa_json_parse",
                    parameters,
                    1
            );

            if (parsed_addr.getValue(0).v.i32 == 0) {
                throw new ExtismException("failed to parse json value");
            }

            return parsed_addr.getValue(0).v.i32;
        }
    }

    public void clean() {
        this.plugin.free();
    }

    public String evalute(String input) {
        int entrypoint = 0;

        int data_addr = loadJSON("{}".getBytes(StandardCharsets.UTF_8));

        IntegerParameter parameter = new IntegerParameter();

        Parameters base_heap_ptr = plugin.call(
                "opa_heap_ptr_get",
                new Parameters(0),
                1
        );

        int data_heap_ptr = base_heap_ptr.getValue(0).v.i32;

        int input_len = input.getBytes(StandardCharsets.UTF_8).length;
        LibExtism.INSTANCE.extism_memory_write_bytes(
                this.plugin.getPointer(),
                this.plugin.getIndex(),
                input.getBytes(StandardCharsets.UTF_8),
                input_len,
                data_heap_ptr
        );

        int heap_ptr = data_heap_ptr + input_len;
        int input_addr = data_heap_ptr;

        Parameters ptr = new Parameters(7);
        parameter.add(ptr, 0, 0);
        parameter.add(ptr, entrypoint, 1);
        parameter.add(ptr, data_addr, 2);
        parameter.add(ptr, input_addr, 3);
        parameter.add(ptr, input_len, 4);
        parameter.add(ptr, heap_ptr, 5);
        parameter.add(ptr, 0, 6);

        Results ret = plugin.call("opa_eval", ptr, 1);

        Pointer memory = LibExtism.INSTANCE.extism_get_memory(
                plugin.getPointer(),
                plugin.getIndex(),
                "memory");

        byte[] mem = memory.getByteArray(ret.getValue(0).v.i32, 65356);
        int size = lastValidByte(mem);

        String res = new String(Arrays.copyOf(mem, size), StandardCharsets.UTF_8);

//        plugin.free();

        plugin.freeResults(ret);
        return res;
    }

    int lastValidByte(byte[] arr) {
        for(int i=0; i<arr.length; i++) {
            if (arr[i] == 0) {
                return i;
            }
        }
        return arr.length;
    }

    String dumpJSON() {
        Results addr = plugin.call("builtins",  new Parameters(0), 1);

        Parameters parameters = new Parameters(1);
        IntegerParameter builder = new IntegerParameter();
        builder.add(parameters, addr.getValue(0).v.i32, 0);

        Results rawAddr = plugin.call("opa_json_dump", parameters, 1);

        Pointer memory = LibExtism.INSTANCE.extism_get_memory(plugin.getPointer(), plugin.getIndex(), "memory");
        byte[] mem = memory.getByteArray(rawAddr.getValue(0).v.i32, 65356);
        int size = lastValidByte(mem);

        return new String(Arrays.copyOf(mem, size), StandardCharsets.UTF_8);
    }

    public String rawBytePtrToString(ExtismCurrentPlugin plugin, long offset, int arrSize) {
        var memoryLength = LibExtism.INSTANCE.extism_current_plugin_memory_length(plugin.pointer, arrSize);
        var arr = plugin.memory().share(offset, memoryLength)
                .getByteArray(0, arrSize);
        return new String(arr, StandardCharsets.UTF_8);
    }
}













