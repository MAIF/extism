package org.extism.sdk;

import com.sun.jna.Pointer;
import org.extism.sdk.manifest.Manifest;
import org.extism.sdk.manifest.MemoryOptions;
import org.extism.sdk.wasm.WasmSourceResolver;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.extism.sdk.TestWasmSources.CODE;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PluginTests {

    // static {
    //     Extism.setLogFile(Paths.get("/tmp/extism.log"), Extism.LogLevel.TRACE);
    // }

    @Test
    public void shouldInvokeFunctionWithMemoryOptions() {
        //FIXME check whether memory options are effective
        var manifest = new Manifest(List.of(CODE.pathWasmSource()), new MemoryOptions(0));
        var output = Extism.invokeFunction(manifest, "count_vowels", "Hello World");
        assertThat(output).isEqualTo("{\"count\": 3}");
    }

    @Test
    public void shouldInvokeFunctionWithConfig() {
        //FIXME check if config options are available in wasm call
        var config = Map.of("key1", "value1");
        var manifest = new Manifest(List.of(CODE.pathWasmSource()), null, config);
        var output = Extism.invokeFunction(manifest, "count_vowels", "Hello World");
        assertThat(output).isEqualTo("{\"count\": 3}");
    }

    @Test
    public void shouldInvokeFunctionFromFileWasmSource() {
        var manifest = new Manifest(CODE.pathWasmSource());
        var output = Extism.invokeFunction(manifest, "count_vowels", "Hello World");
        assertThat(output).isEqualTo("{\"count\": 3}");
    }

    // TODO This test breaks on CI with error:
    // data did not match any variant of untagged enum Wasm at line 8 column 3
    // @Test
    // public void shouldInvokeFunctionFromByteArrayWasmSource() {
    //     var manifest = new Manifest(CODE.byteArrayWasmSource());
    //     var output = Extism.invokeFunction(manifest, "count_vowels", "Hello World");
    //     assertThat(output).isEqualTo("{\"count\": 3}");
    // }

    @Test
    public void shouldFailToInvokeUnknownFunction() {
        assertThrows(ExtismException.class, () -> {
            var manifest = new Manifest(CODE.pathWasmSource());
            Extism.invokeFunction(manifest, "unknown", "dummy");
        }, "Function not found: unknown");
    }

    @Test
    public void shouldAllowInvokeFunctionFromFileWasmSourceMultipleTimes() {
        var wasmSource = CODE.pathWasmSource();
        var manifest = new Manifest(wasmSource);
        var output = Extism.invokeFunction(manifest, "count_vowels", "Hello World");
        assertThat(output).isEqualTo("{\"count\": 3}");

        output = Extism.invokeFunction(manifest, "count_vowels", "Hello World");
        assertThat(output).isEqualTo("{\"count\": 3}");
    }

    @Test
    public void shouldAllowInvokeFunctionFromFileWasmSourceApiUsageExample() {

        var wasmSourceResolver = new WasmSourceResolver();
        var manifest = new Manifest(wasmSourceResolver.resolve(CODE.getWasmFilePath()));

        var functionName = "count_vowels";
        var input = "Hello World";

        try (var ctx = new Context()) {
            try (var plugin = ctx.newPlugin(manifest, false, null)) {
                var output = plugin.call(functionName, input);
                assertThat(output).isEqualTo("{\"count\": 3}");
            }
        }
    }

    @Test
    public void shouldAllowInvokeFunctionFromFileWasmSourceMultipleTimesByReusingContext() {
        var manifest = new Manifest(CODE.pathWasmSource());
        var functionName = "count_vowels";
        var input = "Hello World";

        try (var ctx = new Context()) {
            try (var plugin = ctx.newPlugin(manifest, false, null)) {
                var output = plugin.call(functionName, input);
                assertThat(output).isEqualTo("{\"count\": 3}");

                output = plugin.call(functionName, input);
                assertThat(output).isEqualTo("{\"count\": 3}");
            }
        }
    }

    @Test
    public void shouldAllowInvokeHostFunctionFromPDK() {
        var parametersTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};
        var resultsTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};

        class MyUserData extends HostUserData {
            private String data1;
            private int data2;

            public MyUserData(String data1, int data2) {
                super();
                this.data1 = data1;
                this.data2 = data2;
            }
        }

        ExtismFunction helloWorldFunction = (ExtismFunction<MyUserData>) (plugin, params, returns, data) -> {
            System.out.println("Hello from Java Host Function!");
            System.out.println(String.format("Input string received from plugin, %s", plugin.inputString(params[0])));

            int offs = plugin.alloc(4);
            Pointer mem = plugin.memory();
            mem.write(offs, "test".getBytes(), 0, 4);
            returns[0].v.i64 = offs;

            data.ifPresent(d -> System.out.println(String.format("Host user data, %s, %d", d.data1, d.data2)));
        };

        HostFunction helloWorld = new HostFunction<>(
                "hello_world",
                parametersTypes,
                resultsTypes,
                helloWorldFunction,
                Optional.of(new MyUserData("test", 2))
        );

        HostFunction[] functions = {helloWorld};

        try (var ctx = new Context()) {
            Manifest manifest = new Manifest(Arrays.asList(CODE.pathWasmFunctionsSource()));
            String functionName = "count_vowels";

            try (var plugin = ctx.newPlugin(manifest, true, functions)) {
                var output = plugin.call(functionName, "this is a test");
                assertThat(output).isEqualTo("test");
            }
        }
    }

    @Test
    public void shouldAllowInvokeHostFunctionWithoutUserData() {
        var parametersTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};
        var resultsTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};


        ExtismFunction helloWorldFunction = (plugin, params, returns, data) -> {
            System.out.println("Hello from Java Host Function!");
            System.out.println(String.format("Input string received from plugin, %s", plugin.inputString(params[0])));

            int offs = plugin.alloc(4);
            Pointer mem = plugin.memory();
            mem.write(offs, "test".getBytes(), 0, 4);
            returns[0].v.i64 = offs;

            assertThat(data.isEmpty());
        };

        HostFunction helloWorld = new HostFunction<>(
                "hello_world",
                parametersTypes,
                resultsTypes,
                helloWorldFunction,
                Optional.empty()
        );

        HostFunction[] functions = {helloWorld};

        try (var ctx = new Context()) {
            Manifest manifest = new Manifest(Arrays.asList(CODE.pathWasmFunctionsSource()));
            String functionName = "count_vowels";

            try (var plugin = ctx.newPlugin(manifest, true, functions)) {
                var output = plugin.call(functionName, "this is a test");
                assertThat(output).isEqualTo("test");
            }
        }
    }

    @Test
    public void shouldFailToInvokeUnknownHostFunction() {
        try (var ctx = new Context()) {
            Manifest manifest = new Manifest(Arrays.asList(CODE.pathWasmFunctionsSource()));
            String functionName = "count_vowels";

            try {
                var plugin = ctx.newPlugin(manifest, true, null);
                plugin.call(functionName, "this is a test");
            }  catch (ExtismException e) {
                assertThat(e.getMessage()).contains("unknown import: `env::hello_world` has not been defined");
            }
        }
    }

    @Test
    public void shouldRunOPAPolicy() {
        try (var ctx = new Context()) {
            Manifest manifest = new Manifest(Arrays.asList(CODE.pathWasmFunctionsSource()));
            String functionName = "eval";

            ExtismFunction opaAbortFunction = (plugin, params, returns, data) -> {};
            ExtismFunction opaPrintlnFunction = (plugin, params, returns, data) -> {};
            ExtismFunction opaBuiltin0Function = (plugin, params, returns, data) -> {};
            ExtismFunction opaBuiltin1Function = (plugin, params, returns, data) -> {};
            ExtismFunction opaBuiltin2Function = (plugin, params, returns, data) -> {};
            ExtismFunction opaBuiltin3Function = (plugin, params, returns, data) -> {};
            ExtismFunction opaBuiltin4Function = (plugin, params, returns, data) -> {};

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

            var plugin = ctx.newPlugin(manifest, true, functions);
            plugin.call(functionName, "this is a test");
        }
    }
}
