package org.extism.sdk;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.extism.sdk.coraza.proxywasm.VMData;
import org.extism.sdk.coraza.proxywasmhost.ProxyWasmPlugin;
import org.extism.sdk.manifest.Manifest;
import org.extism.sdk.manifest.MemoryOptions;
import org.extism.sdk.support.JsonSerde;
import org.extism.sdk.wasmotoroshi.*;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.extism.sdk.TestWasmSources.CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WasmOtoroshiTests {

    @Test
    public void shouldWorks() {
        Manifest manifest = new Manifest(Collections.singletonList(CODE.getRawAdditionPath()));

        LibExtism.ExtismValType[] parametersTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};
        LibExtism.ExtismValType[] resultsTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};

        ExtismFunction<HostUserData> helloWorldFunction = (plugin, params, returns, data) -> {
            System.out.println("Hello from Java Host Function!");
        };

        var f = new HostFunction<>(
                "hello_world",
                parametersTypes,
                resultsTypes,
                helloWorldFunction,
                Optional.empty()
        ).withNamespace("env");

        var functions = new HostFunction[]{f};

        List<Integer> test = new ArrayList<>(500);
        for (int i = 0; i < 500; i++) {
            test.add(i);
        }

        try(var instance = new Plugin(manifest, true, functions)) {
            test.parallelStream().forEach(number -> {
                try(var params = new Parameters(2)
                        .pushInts(2, 3)) {

                    Results result = instance.call("add", params, 1);
                    assertEquals(result.getValue(0).v.i32, 5);
                }
            });
        }
    }

    @Test
    public void shouldExistmCallWorks() throws Exception {
        var manifest = new Manifest(Collections.singletonList(CODE.pathWasmWebAssemblyFunctionSource()));

        LibExtism.ExtismValType[] parametersTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};
        LibExtism.ExtismValType[] resultsTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};

        var functions = new HostFunction[]{
                new HostFunction<>(
                        "hello_world",
                        parametersTypes,
                        resultsTypes,
                        (plugin, params, returns, data) -> {
                            System.out.println("Hello from Java Host Function !!");
                }, Optional.empty()
                ).withNamespace("env")
        };

        try (var instance = new Plugin(manifest, true, functions)) {
            instance.call("execute", "".getBytes(StandardCharsets.UTF_8));
        }
    }


    @Test
    public void shouldInvokeNativeFunction() {
        Manifest manifest = new Manifest(Arrays.asList(CODE.getRawAdditionPath()));
        String functionName = "add";

        Parameters params = new Parameters(2)
                .pushInts(2, 3);

        try (var instance = new Plugin(manifest, true, null, null)) {
            Results result = instance.call(functionName, params, 1);
            assertEquals(result.getValues()[0].v.i32, 5);

        }
    }

    int lastValidByte(byte[] arr) {
        for(int i=0; i<arr.length; i++) {
            if (arr[i] == 0) {
                return i;
            }
        }
        return arr.length;
    }

//    @Test
//    public void shouldSucceedInCreatingLinearMemoryWithCustomNamespace() {
//        var manifest = new Manifest(Collections.singletonList(CODE.pathWasmWebAssemblyFunctionSource()));
//
//        var parametersTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};
//        var resultsTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};
//
//        var functions = new HostFunction[]{
//                new HostFunction<>(
//                        "hello_world",
//                        parametersTypes,
//                        resultsTypes,
//                        (plugin, params, returns, data) -> {
//                            var memory = plugin.getLinearMemory("foo", "bar");
//
//                            var arraySize = 65356;
//                            var mem = memory.getByteArray(0, arraySize);
//                            var size = lastValidByte(mem);
//
//                            assertEquals("foo bar message", new String(Arrays.copyOf(mem, size), StandardCharsets.UTF_8));
//                            System.out.println("Hello from Java Host Function!");
//                        },
//                        Optional.empty()
//                ).withNamespace("env")
//        };
//
//        var memory = new LinearMemory("foo", "bar", new LinearMemoryOptions(1, Optional.empty()));
//
//        try(var instance = new Plugin(manifest, true, functions, new LinearMemory[]{
//                memory
//        })) {
//            var message = "foo bar message";
//            instance.writeBytes(message.getBytes(StandardCharsets.UTF_8), message.length(), 0, "bar", "foo");
//
//            instance.call("execute", "".getBytes(StandardCharsets.UTF_8));
//            instance.reset();
//
//            var linearMemory = instance.getMemory("foo", "bar");
//            var arraySize = 65356;
//            var mem = linearMemory.getByteArray(0, arraySize);
//            var size = lastValidByte(mem);
//            assertEquals("foo bar message", new String(Arrays.copyOf(mem, size), StandardCharsets.UTF_8));
//
//            instance.call("execute", "".getBytes(StandardCharsets.UTF_8));
//            instance.reset();
//        }
//    }

    @Test
    public void shouldGetMemoryBounds() {
        Manifest manifest = new Manifest(Collections.singletonList(CODE.pathWasmWebAssemblyFunctionSource()), new MemoryOptions(4));

        LibExtism.ExtismValType[] parametersTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};
        LibExtism.ExtismValType[] resultsTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};

        HostFunction[] functions = {
                new HostFunction<>(
                        "hello_world",
                        parametersTypes,
                        resultsTypes,
                        (plugin, params, returns, data) -> {
                            System.out.println("Hello from Java Host Function!");
                        },
                        Optional.empty()
                ).withNamespace("env")
        };

        var instance = new Plugin(manifest, true, functions, new LinearMemory[0]);
        instance.call("execute", "".getBytes(StandardCharsets.UTF_8));
        instance.free();
    }

    @Test
    public void shouldCreateLinearMemory() {
        var manifest = new Manifest(Collections.singletonList(CODE.pathWasmWebAssemblyFunctionSource()), new MemoryOptions(4));
        LibExtism.ExtismValType[] parametersTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};
        LibExtism.ExtismValType[] resultsTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};

        var functions = new HostFunction[]{
                new HostFunction<>(
                        "hello_world",
                        parametersTypes,
                        resultsTypes,
                        (plugin, params, returns, data) -> {
                            System.out.println("Hello from Java Host Function!");
                        },
                        Optional.empty()
                ).withNamespace("env")
        };

        var memory = new LinearMemory("huge-memory", new LinearMemoryOptions(0, Optional.of(2)));

        var instance = new Plugin(manifest, true, functions, new LinearMemory[]{memory});
        instance.call("execute", "".getBytes(StandardCharsets.UTF_8));
        instance.free();
    }

    @Test
    public void shouldPluginWithNewVersionRun() {
        var manifest = new Manifest(Collections.singletonList(CODE.getMajorRelease()), new MemoryOptions(50));

        var instance = new Plugin(manifest, true, null);
        instance.call("execute", "{}".getBytes(StandardCharsets.UTF_8));
        instance.free();
    }

    @Test
    public void shouldOPAWorks() {
        var opa = new OPA(CODE.getOPA());

        var values = opa.initialize();
        var result = opa.evaluate(
                (int)values.toArray()[0],
                (int)values.toArray()[1],
                "{\n" +
                        "    \"request\": {\n" +
                        "        \"headers\": {\n" +
                        "            \"foo\": \"bar\"\n" +
                        "        }\n" +
                        "    }\n" +
                        "}");

        assertEquals("[{\"result\":true}]", result);

        result = opa.evaluate(
                (int)values.toArray()[0],
                (int)values.toArray()[1],
                "{\n" +
                        "    \"request\": {\n" +
                        "        \"headers\": {\n" +
                        "            \"foo\": \"asdas\"\n" +
                        "        }\n" +
                        "    }\n" +
                        "}");

        assertEquals("[{\"result\":false}]", result);
    }

    @Test
    public void corazaShouldWorks() {
        Manifest manifest = new Manifest(Arrays.asList(CODE.pathWasmWaf()), new MemoryOptions(20000));

        Map<String, byte[]> headers = new HashMap<>() {{
            put("path", "/admin?arg=<script>alert(0)</script>".getBytes(StandardCharsets.UTF_8));
            put("url_path", "/admin".getBytes(StandardCharsets.UTF_8));
            put("host", "localhost".getBytes(StandardCharsets.UTF_8));
            put("scheme", "http".getBytes(StandardCharsets.UTF_8));
            put("method", "GET".getBytes(StandardCharsets.UTF_8));
            put("headers", "".getBytes(StandardCharsets.UTF_8));
            put("referer", "".getBytes(StandardCharsets.UTF_8));
            put("useragent", "".getBytes(StandardCharsets.UTF_8));
            put("time", "".getBytes(StandardCharsets.UTF_8));
            put("id", "x-request-id".getBytes(StandardCharsets.UTF_8));
            put("protocol", "HTTP/1.1".getBytes(StandardCharsets.UTF_8));
            put("query", "?arg=<script>alert(0)</script>".getBytes(StandardCharsets.UTF_8));
        }};

        VMData data = new VMData(headers);
        var plugin = new ProxyWasmPlugin(manifest, data);

        plugin.start();
        plugin.run();
        plugin.plugin.resetCustomMemory();
        plugin.start();
        plugin.run();

        plugin.plugin.free();
    }

    @Test
    public void getEnvMemorySize() {
         var manifest = new Manifest(Collections.singletonList(CODE.pathWasmWebAssemblyFunctionSource()));

         var message = "foo bar message";
         String namespace = "env";
         String name = "memory";

        var parametersTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};
        var resultsTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};

        var functions = new HostFunction[]{
                new HostFunction<>(
                        "hello_world",
                        parametersTypes,
                        resultsTypes,
                        (plugin, params, returns, data) -> {
                            System.out.println("Hello from Java Host Function!");

                            var memory = plugin.linearMemoryGet(namespace, name);
                            var arraySize = 65356;
                            var mem = memory.getByteArray(0, arraySize);
                            var size = lastValidByte(mem);
                            assertEquals(message, new String(Arrays.copyOf(mem, size), StandardCharsets.UTF_8));
                        },
                        Optional.empty()
                ).withNamespace(namespace)
        };

        var memory = new LinearMemory(name, namespace, new LinearMemoryOptions(1, Optional.empty()));

        try(var instance = new Plugin(manifest, true, functions, new LinearMemory[]{
                memory
        })) {
            instance.writeBytes(message.getBytes(StandardCharsets.UTF_8), message.length(), 0, namespace, name);
            instance.call("execute", "".getBytes(StandardCharsets.UTF_8));

            System.out.println("Linear memory size : " + instance.getLinearMemorySize(namespace, name));
            instance.resetLinearMemory(namespace, name);

            checkLinearMemorySize(instance, namespace, name);
        }
    }

    void checkLinearMemorySize(Plugin instance, String namespace, String name) {
        var linearMemory = instance.getLinearMemory(namespace, name);
        var arraySize = 65356;
        var mem = linearMemory.getByteArray(0, arraySize);
        var size = lastValidByte(mem);
        assertEquals("", new String(Arrays.copyOf(mem, size), StandardCharsets.UTF_8));
    }

    @Test
    public void shoudLogging() {
        System.out.println("Log from JAVA");
        Manifest manifest = new Manifest(Arrays.asList(CODE.getLogging()));

        var instance = new Plugin(manifest, true, null);
        instance.call("greet", "Super ca va".getBytes(StandardCharsets.UTF_8));

        LibExtism.INSTANCE.restore_stdout(instance.pluginPointer);

        System.out.println("Last Log from JAVA");
    }
}
