package org.extism.sdk;

import com.sun.jna.Pointer;
import org.assertj.core.groups.Tuple;
import org.extism.sdk.manifest.Manifest;
import org.extism.sdk.manifest.MemoryOptions;
import org.extism.sdk.wasm.WasmSource;
import org.extism.sdk.wasmotoroshi.LinearMemory;
import org.extism.sdk.wasmotoroshi.LinearMemoryOptions;
import org.extism.sdk.wasmotoroshi.Parameters;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class OPA {
    private Plugin plugin;

    public OPA(Pointer engine, WasmSource regoWasm) {
        Manifest manifest = new Manifest(Collections.singletonList(regoWasm), new MemoryOptions(2000));

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
                engine,
                "opa_abort",
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32},
                new LibExtism.ExtismValType[0],
                opaAbortFunction,
                Optional.empty()
        ).withNamespace("env");
        HostFunction opa_println = new HostFunction<>(
                engine,
                "opa_println",
                parametersTypes,
                resultsTypes,
                opaPrintlnFunction,
                Optional.empty()
        ).withNamespace("env");
        HostFunction opa_builtin0 = new HostFunction<>(
                engine,
                "opa_builtin0",
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32},
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32},
                opaBuiltin0Function,
                Optional.empty()
        ).withNamespace("env");
        HostFunction opa_builtin1 = new HostFunction<>(
                engine,
                "opa_builtin1",
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32},
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32},
                opaBuiltin1Function,
                Optional.empty()
        ).withNamespace("env");
        HostFunction opa_builtin2 = new HostFunction<>(
                engine,
                "opa_builtin2",
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32},
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32},
                opaBuiltin2Function,
                Optional.empty()
        ).withNamespace("env");
        HostFunction opa_builtin3 = new HostFunction<>(
                engine,
                "opa_builtin3",
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32},
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32},
                opaBuiltin3Function,
                Optional.empty()
        ).withNamespace("env");
        HostFunction opa_builtin4 = new HostFunction<>(
                engine,
                "opa_builtin4",
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32},
                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32},
                opaBuiltin4Function,
                Optional.empty()
        ).withNamespace("env");

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
                new LinearMemory("memory", "env", new LinearMemoryOptions(4, Optional.of(100)))
        };

        this.plugin = new Plugin(engine, manifest, true, functions, memories);
    }

    String loadJSON(byte[] value) {
        if (value.length == 0) {
            return "0";
        } else {
            var value_buf_len = value.length;
            var parameters    = new Parameters(1)
                    .pushInt(value_buf_len);

            var raw_addr = plugin.call("opa_malloc", parameters, 1);

            if (
                    plugin.writeBytes(
                            value,
                            value_buf_len,
                            raw_addr.getValue(0).v.i32,
                            "env",
                            "memory"
                    ) == -1
            ) {
                return "Cant' write in memory";
            } else {
                parameters = new Parameters(2)
                        .pushInts(raw_addr.getValue(0).v.i32, value_buf_len);
                var parsed_addr = plugin.call(
                        "opa_json_parse",
                        parameters,
                        1
                );

                if (parsed_addr.getValue(0).v.i32 == 0) {
                    return "failed to parse json value";
                } else {
                    return String.valueOf(parsed_addr.getValue(0).v.i32);
                }
            }
        }
    }

    Tuple initialize() {
        var dataAddr = loadJSON("{}".getBytes(StandardCharsets.UTF_8));

        var base_heap_ptr = plugin.call(
        "opa_heap_ptr_get",
        new Parameters(0),
        1
        );

        var data_heap_ptr = base_heap_ptr.getValue(0).v.i32;

        return new Tuple(Integer.parseInt(dataAddr), data_heap_ptr);
    }

    String evaluate(
            int dataAddr,
            int baseHeapPtr,
            String input
    ) {
        var entrypoint = 0;

        // TODO - read and load builtins functions by calling dumpJSON
        var input_len = input.getBytes(StandardCharsets.UTF_8).length;
        plugin.writeBytes(
                input.getBytes(StandardCharsets.UTF_8),
                input_len,
                baseHeapPtr,
                "env",
                "memory"
        );

        var heap_ptr   = baseHeapPtr + input_len;
        var input_addr = baseHeapPtr;

        var ptr = new Parameters(7)
                .pushInts(0, entrypoint, dataAddr, input_addr, input_len, heap_ptr, 0);

        var ret = plugin.call("opa_eval", ptr, 1);

        var memory = plugin.getLinearMemory("env", "memory");

        var offset    = ret.getValue(0).v.i32;
        var arraySize = 65356;

        var mem = memory.getByteArray(offset, arraySize);
        var size = lastValidByte(mem);

        return new String(java.util.Arrays.copyOf(mem, size), StandardCharsets.UTF_8);
    }

    int lastValidByte(byte[] arr) {
        for(int i=0; i<arr.length; i++) {
            if (arr[i] == 0) {
                return i;
            }
        }
        return arr.length;
    }
}