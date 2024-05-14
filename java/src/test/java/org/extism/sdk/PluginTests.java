package org.extism.sdk;

import com.sun.jna.Pointer;
import org.extism.sdk.manifest.Manifest;
import org.extism.sdk.manifest.MemoryOptions;
import org.extism.sdk.wasm.WasmSourceResolver;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

        Pointer engine = LibExtism.INSTANCE.create_engine();

        try (var plugin = new Plugin(engine, manifest, false, null)) {
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

        Pointer engine = LibExtism.INSTANCE.create_engine();

        HostFunction helloWorld = new HostFunction<>(
                engine,
                "hello_world",
                parametersTypes,
                resultsTypes,
                helloWorldFunction,
                Optional.of(new MyUserData("test", 2))
        );

        HostFunction[] functions = {helloWorld};

        Manifest manifest = new Manifest(Arrays.asList(CODE.pathWasmFunctionsSource()));
        String functionName = "count_vowels";

        try (var plugin = new Plugin(engine, manifest, true, functions)) {
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

        Pointer engine = LibExtism.INSTANCE.create_engine();

        HostFunction f = new HostFunction<>(
                engine,
                "hello_world",
                parametersTypes,
                resultsTypes,
                helloWorldFunction,
                Optional.empty()
        )
                .withNamespace("extism:host/user");

        HostFunction g = new HostFunction<>(
                engine,
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

        try (var plugin = new Plugin(engine, manifest, true, functions)) {
            var output = plugin.call(functionName, "this is a test");
            assertThat(output).isEqualTo("test");
        }
    }


    @Test
    public void shouldFailToInvokeUnknownHostFunction() {
        Manifest manifest = new Manifest(Arrays.asList(CODE.pathWasmFunctionsSource()));
        String functionName = "count_vowels";

        Pointer engine = LibExtism.INSTANCE.create_engine();

        try {
            var plugin = new Plugin(engine, manifest, true, null);
            plugin.call(functionName, "this is a test");
        }  catch (ExtismException e) {
            assertThat(e.getMessage()).contains("Unable to create Extism plugin: unknown import: `extism:host/user::hello_world` has not been defined");
        }
    }
}
