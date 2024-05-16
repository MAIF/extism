package org.extism.sdk;

import com.sun.jna.Pointer;
import org.extism.sdk.manifest.Manifest;
import org.extism.sdk.manifest.MemoryOptions;
import org.extism.sdk.wasm.WasmSourceResolver;
import org.extism.sdk.wasmotoroshi.Parameters;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.extism.sdk.TestWasmSources.CODE;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PluginTests {

    @Test
    public void shouldInvokeFunctionWithMemoryOptions() {
        var manifest = new Manifest(List.of(CODE.pathWasmSource()), new MemoryOptions(0));
        assertThrows(ExtismException.class, () -> {
            Extism.invokeFunction(manifest, "count_vowels", "Hello World");
        });
    }

    @Test
    public void shouldInvokeFunctionWithConfig() {
        //FIXME check if config options are available in wasm call
        var config = Map.of("key1", "value1");
        var manifest = new Manifest(List.of(CODE.pathWasmSource()), null, config);
        var output = Extism.invokeFunction(manifest, "count_vowels", "Hello World");
        assertThat(output).startsWith("{\"count\":3,\"total\":3,\"vowels\":\"aeiouAEIOU\"}");
    }

    @Test
    public void shouldInvokeFunctionFromFileWasmSource() {
        var manifest = new Manifest(CODE.pathWasmSource());
        var output = Extism.invokeFunction(manifest, "count_vowels", "Hello World");
        assertThat(output).isEqualTo("{\"count\":3,\"total\":3,\"vowels\":\"aeiouAEIOU\"}");
    }

     @Test
     public void shouldInvokeFunctionFromByteArrayWasmSource() {
         var manifest = new Manifest(CODE.byteArrayWasmSource());
         var output = Extism.invokeFunction(manifest, "count_vowels", "Hello World");
         assertThat(output).isEqualTo("{\"count\":3,\"total\":3,\"vowels\":\"aeiouAEIOU\"}");
     }

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
        assertThat(output).isEqualTo("{\"count\":3,\"total\":3,\"vowels\":\"aeiouAEIOU\"}");

        output = Extism.invokeFunction(manifest, "count_vowels", "Hello World");
        assertThat(output).isEqualTo("{\"count\":3,\"total\":3,\"vowels\":\"aeiouAEIOU\"}");
    }

    @Test
    public void shouldAllowInvokeFunctionFromFileWasmSourceApiUsageExample() throws Exception {

        var wasmSourceResolver = new WasmSourceResolver();
        var manifest = new Manifest(wasmSourceResolver.resolve(CODE.getWasmFilePath()));

        var functionName = "count_vowels";
        var input = "Hello World";

        try (var plugin = new Plugin(manifest, false, null)) {
            var output = plugin.call(functionName, input);
            assertThat(output).isEqualTo("{\"count\":3,\"total\":3,\"vowels\":\"aeiouAEIOU\"}");
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

        Manifest manifest = new Manifest(Arrays.asList(CODE.pathWasmFunctionsSource()));
        String functionName = "count_vowels";

        try (var plugin = new Plugin(manifest, true, functions)) {
            var output = plugin.call(functionName, "this is a test");
            assertThat(output).isEqualTo("test");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void shouldAllowInvokeHostFunctionWithoutUserData() throws Exception {

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

        HostFunction f = new HostFunction<>(
                "hello_world",
                parametersTypes,
                resultsTypes,
                helloWorldFunction,
                Optional.empty()
        )
                .withNamespace("extism:host/user");

        HostFunction g = new HostFunction<>(
                "hello_world",
                parametersTypes,
                resultsTypes,
                helloWorldFunction,
                Optional.empty()
        )
                .withNamespace("test");

        HostFunction[] functions = {f,g};

        Manifest manifest = new Manifest(Arrays.asList(CODE.pathWasmFunctionsSource()));
        String functionName = "count_vowels";

        try (var plugin = new Plugin(manifest, true, functions)) {
            var output = plugin.call(functionName, "this is a test");
            assertThat(output).isEqualTo("test");
        }
    }


    @Test
    public void shouldFailToInvokeUnknownHostFunction() {
        Manifest manifest = new Manifest(Arrays.asList(CODE.pathWasmFunctionsSource()));
        String functionName = "count_vowels";

        try {
            var plugin = new Plugin(manifest, true, null);
            plugin.call(functionName, "this is a test");
        }  catch (ExtismException e) {
            assertThat(e.getMessage()).contains("Unable to create Extism plugin: unknown import: `extism:host/user::hello_world` has not been defined");
        }
    }

    @Test
    public void shouldHttpWasmWorking() {

        var getUriParamaters    = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32, LibExtism.ExtismValType.I32};
        var getUriReturns       = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32};

        ExtismFunction getUriFunction = (plugin, params, returns, data) -> {
            System.out.println(params[0]);
            String foo = "/foo";
            System.out.println("get_uri");

            var memory = plugin.memory();
            memory.write(params[0].v.i32, foo.getBytes(StandardCharsets.UTF_8), 0, foo.length());

            System.out.println(new String(memory.getByteArray(params[0].v.i32, 4)));

            returns[0].v.i32 = foo.length();
            System.out.println("ending get_uri");
        };

        var functions = new HostFunction[]{
                new HostFunction<>(
                        "log_enabled",
                        new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32},
                        new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32},
                        (plugin, params, returns, data) -> {
                            System.out.println("log_enabled");

//                            returns[0].v.i32 = 0;
                            // We expect debug logging to be disabled. Panic otherwise!
                            if (params[0].v.i32 != -1) {
                                returns[0].v.i32 = 1;
                            } else {
                                returns[0].v.i32 = 0;
                            }
                            System.out.println("Ending log_enabled");
                }, Optional.empty()
                ).withNamespace("http_handler"),

                new HostFunction<>(
                        "log",
                        new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32, LibExtism.ExtismValType.I32},
                        new LibExtism.ExtismValType[]{},
                        (plugin, params, returns, data) -> {
                            System.out.println("LOGGING at : " + params[0].v.i32);


                        }, Optional.empty()
                ).withNamespace("http_handler"),

                new HostFunction<>(
                        "get_config",
                        new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32},
                        new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32},
                        (plugin, params, returns, data) -> {
                            System.out.println("get_config");

                            var offset = params[0].v.i32;
                            var limit = params[1].v.i32;

//                            val vLen = v.length
//                            if (vLen > limit || vLen == 0) {
//                              return vLen
//                            }

//                            var memory = plugin.customMemoryGet();
//                            memory.write(offset, v.toArray, 0, vLen);

                            returns[0].v.i32 = 8;

                        }, Optional.empty()
                ).withNamespace("http_handler"),
                new HostFunction<>(
                        "enable_features",
                        new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32},
                        new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32},
                        (plugin, params, returns, data) -> {
                            System.out.println("enable_features");

                            returns[0].v.i32 = 0;

                        }, Optional.empty()
                ).withNamespace("http_handler"),
                new HostFunction<>(
                        "get_uri",
                        getUriParamaters,
                        getUriReturns,
                        getUriFunction, Optional.empty()
                ).withNamespace("http_handler"),
                new HostFunction<>(
                        "set_uri",
                        new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32, LibExtism.ExtismValType.I32},
                        new LibExtism.ExtismValType[]{},
                        (plugin, params, returns, data) -> System.out.println("set_uri"), Optional.empty()
                ).withNamespace("http_handler"),
                new HostFunction<>(
                        "set_header_value",
                        new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32, LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32},
                        new LibExtism.ExtismValType[]{},
                        (plugin, params, returns, data) -> System.out.println("set_header_value"), Optional.empty()
                ).withNamespace("http_handler"),
                new HostFunction<>(
                        "write_body",
                        new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32,LibExtism.ExtismValType.I32},
                        new LibExtism.ExtismValType[]{},
                        (plugin, params, returns, data) -> System.out.println("write_body"), Optional.empty()
                ).withNamespace("http_handler")
        };


        Manifest manifest = new Manifest(Arrays.asList(CODE.getLog()));

        var plugin = new Plugin(manifest, false, functions);
        System.out.println(plugin.callWithoutParams("handle_request", 1));

//        System.out.println(plugin.callWithoutParams("handle_response", 1));

        System.out.println("ending test");
    }
}
