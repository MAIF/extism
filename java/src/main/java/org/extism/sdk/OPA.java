package org.extism.sdk;

import com.google.gson.GsonBuilder;
import com.sun.jna.Pointer;
import org.extism.sdk.manifest.Manifest;
import org.extism.sdk.manifest.MemoryOptions;
import org.extism.sdk.wasm.WasmSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

public class OPA {
    private Plugin plugin;

    public OPA(Context ctx, WasmSource regoWasm, MemoryOptions memoryDescriptor) {
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


        this.plugin = ctx.newPlugin(manifest, true, functions);

        String builtinsFunctions = dumpJSON();
        System.out.println("Required builtins fuctions : " + (builtinsFunctions.isEmpty() ? "None" : builtinsFunctions));
//
//        this.plugin.evaluate();

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
            LibExtism.ExtismVal.ByReference raw_addr_params = new LibExtism.ExtismVal.ByReference();
            LibExtism.ExtismVal[] params = (LibExtism.ExtismVal []) raw_addr_params.toArray(1);
            params[0].t = 0;
            params[0].v.setType(Integer.TYPE);
            params[0].v.i32 = value_buf_len;

            raw_addr_params.write();

            int raw_addr = plugin.callWithIntResult("opa_malloc", raw_addr_params, 1);

            LibExtism.INSTANCE.extism_plugin_memory_write_bytes(
                this.plugin.getPointer(),
                this.plugin.getIndex(),
                value,
                value_buf_len,
                raw_addr
            );

            int parsed_addr = this.plugin.callFunctionWithTwoInts(
                    "opa_json_parse",
                    raw_addr,
                    value_buf_len
            );

            if (parsed_addr == 0) {
                throw new ExtismException("failed to parse json value");
            }

            return parsed_addr;
        }
    }

    public void evalute(String input) {
        int entrypoint = 0;

        int data_addr = loadJSON("{}".getBytes(StandardCharsets.UTF_8));

        int base_heap_ptr = plugin.callWithIntResult(
                "opa_heap_ptr_get",
                new LibExtism.ExtismVal.ByReference(),
                0
        );

        int data_heap_ptr = base_heap_ptr;

        int input_len = input.getBytes(StandardCharsets.UTF_8).length;
        LibExtism.INSTANCE.extism_plugin_memory_write_bytes(
                this.plugin.getPointer(),
                this.plugin.getIndex(),
                input.getBytes(StandardCharsets.UTF_8),
                input_len,
                data_heap_ptr
        );

        int heap_ptr = data_heap_ptr + input_len;
        int input_addr = data_heap_ptr;

        LibExtism.ExtismVal.ByReference ptr = new LibExtism.ExtismVal.ByReference();
        LibExtism.ExtismVal[] params = (LibExtism.ExtismVal []) ptr.toArray(7);
        params[0].t = 0;
        params[0].v.setType(Integer.TYPE);
        params[0].v.i32 = 0;

        params[1].t = 0;
        params[1].v.setType(Integer.TYPE);
        params[1].v.i32 = entrypoint;

        params[2].t = 0;
        params[2].v.setType(Integer.TYPE);
        params[2].v.i32 = data_addr;

        params[3].t = 0;
        params[3].v.setType(Integer.TYPE);
        params[3].v.i32 = input_addr;

        params[4].t = 0;
        params[4].v.setType(Integer.TYPE);
        params[4].v.i32 = input_len;

        params[5].t = 0;
        params[5].v.setType(Integer.TYPE);
        params[5].v.i32 = heap_ptr;

        params[6].t = 0;
        params[6].v.setType(Integer.TYPE);
        params[6].v.i32 = 0;

        ptr.write();

        int ret = plugin.callWithIntResult(
                "opa_eval",
                ptr,
                7);
//
        Pointer memory = LibExtism.INSTANCE.extism_get_memory(plugin.getPointer(), plugin.getIndex(), "memory");

        byte[] mem = memory.getByteArray(ret, 65356);
        int size = lastValidByte(mem);
        String result = new String(Arrays.copyOf(mem, size), StandardCharsets.UTF_8);
        System.out.println(result);
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
        int addr = plugin.callWithIntResult("builtins",  new LibExtism.ExtismVal.ByReference(), 0);

        int rawAddr = plugin.callWithIntResult("opa_json_dump", plugin.intToParams(addr), 1);

        Pointer memory = LibExtism.INSTANCE.extism_get_memory(plugin.getPointer(), plugin.getIndex(), "memory");
        byte[] mem = memory.getByteArray(rawAddr, 65356);
        int size = lastValidByte(mem);
        String result = new String(Arrays.copyOf(mem, size), StandardCharsets.UTF_8);
        return result;
    }

    public String rawBytePtrToString(ExtismCurrentPlugin plugin, long offset, int arrSize) {
        var memoryLength = LibExtism.INSTANCE.extism_current_plugin_memory_length(plugin.pointer, arrSize);
        var arr = plugin.memory().share(offset, memoryLength)
                .getByteArray(0, arrSize);
        return new String(arr, StandardCharsets.UTF_8);
    }
}













