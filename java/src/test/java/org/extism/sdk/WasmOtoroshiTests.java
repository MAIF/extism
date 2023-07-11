package org.extism.sdk;

import com.sun.jna.Pointer;
import org.extism.sdk.manifest.Manifest;
import org.extism.sdk.manifest.MemoryOptions;
import org.extism.sdk.wasmotoroshi.*;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.extism.sdk.TestWasmSources.CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WasmOtoroshiTests {

    @Test
    public void shouldWorks() {
        WasmOtoroshiEngine engine = new WasmOtoroshiEngine();

        Manifest manifest = new Manifest(Collections.singletonList(CODE.getRawAdditionPath()));

        WasmOtoroshiTemplate template = new WasmOtoroshiTemplate(engine, "template-id", manifest);

        WasmBridge.ExtismValType[] parametersTypes = new WasmBridge.ExtismValType[]{WasmBridge.ExtismValType.I64};
        WasmBridge.ExtismValType[] resultsTypes = new WasmBridge.ExtismValType[]{WasmBridge.ExtismValType.I64};

        WasmOtoroshiExtismFunction helloWorldFunction = (plugin, params, returns, data) -> {
            System.out.println("Hello from Java Host Function!");
        };

        WasmOtoroshiHostFunction f = new WasmOtoroshiHostFunction<>(
                "hello_world",
                parametersTypes,
                resultsTypes,
                helloWorldFunction,
                Optional.empty()
        ).withNamespace("env");

        WasmOtoroshiHostFunction[] functions = {f};

        List<Integer> test = new ArrayList<>(500);
        for (int i = 0; i < 500; i++) {
            test.add(i);
        }

        test.parallelStream().forEach(number -> {
            WasmOtoroshiInstance instance = template.instantiate(engine, functions, new WasmOtoroshiLinearMemory[0], true);

            WasmOtoroshiParameters params = new WasmOtoroshiParameters(2)
                .pushInts(2, 3);

            WasmOtoroshiResults result = instance.call("add", params, 1);
            assertEquals(result.getValue(0).v.i32, 5);

            instance.freeResults(result);
            instance.free();
        });

        template.free();
        engine.free();
    }

    @Test
    public void shouldExistmCallWorks() {
        WasmOtoroshiEngine engine = new WasmOtoroshiEngine();

        Manifest manifest = new Manifest(Collections.singletonList(CODE.pathWasmWebAssemblyFunctionSource()));

        WasmOtoroshiTemplate template = new WasmOtoroshiTemplate(engine, "template-id", manifest);

        WasmBridge.ExtismValType[] parametersTypes = new WasmBridge.ExtismValType[]{WasmBridge.ExtismValType.I64};
        WasmBridge.ExtismValType[] resultsTypes = new WasmBridge.ExtismValType[]{WasmBridge.ExtismValType.I64};

        WasmOtoroshiHostFunction[] functions = {
                new WasmOtoroshiHostFunction<>(
                        "hello_world",
                        parametersTypes,
                        resultsTypes,
                        (plugin, params, returns, data) -> {
                            System.out.println("Hello from Java Host Function !!");
                            Pointer p = plugin.getCustomData();

                            CustomDataClass d = new CustomDataClass(p.getPointer(0));
                            d.read();
                            System.out.println(d.a);
                }, Optional.empty()
                ).withNamespace("env")
        };

        WasmOtoroshiInstance instance = template.instantiate(engine, functions, null, true);

        Pointer customData = instance.getCustomData();
        CustomDataClass.ByReference t = new CustomDataClass.ByReference();
        t.a = 23;
        t.write();
        customData.setPointer(0, t.getPointer());

        instance.extismCall("execute", "".getBytes(StandardCharsets.UTF_8));

        t.a = 42;
        t.write();
        customData.setPointer(0, t.getPointer());

        instance.extismCall("execute", "".getBytes(StandardCharsets.UTF_8));

        instance.free();
        template.free();
        engine.free();
    }


    @Test
    public void shouldInvokeNativeFunction() {
        Manifest manifest = new Manifest(Arrays.asList(CODE.getRawAdditionPath()));
        String functionName = "add";

        WasmOtoroshiEngine engine = new WasmOtoroshiEngine();

        WasmOtoroshiTemplate template = new WasmOtoroshiTemplate(engine, "template-id", manifest);

        WasmOtoroshiParameters params = new WasmOtoroshiParameters(2)
                .pushInts(2, 3);

        WasmOtoroshiInstance plugin = template.instantiate(engine, null, null, true);

        WasmOtoroshiResults result = plugin.call(functionName, params, 1);

        assertEquals(result.getValues()[0].v.i32, 5);

        plugin.freeResults(result);
        plugin.free();
        template.free();
        engine.free();
    }


    void display(WasmOtoroshiInternal plugin) {
        Pointer p = plugin.getLinearMemory("memory");

        System.out.println(p);
    }

    @Test
    public void shouldTestWorks() {
        WasmOtoroshiEngine engine = new WasmOtoroshiEngine();

        Manifest manifest = new Manifest(Collections.singletonList(CODE.pathWasmWebAssemblyFunctionSource()));

        WasmOtoroshiTemplate template = new WasmOtoroshiTemplate(engine, "template-id", manifest);

        WasmBridge.ExtismValType[] parametersTypes = new WasmBridge.ExtismValType[]{WasmBridge.ExtismValType.I64};
        WasmBridge.ExtismValType[] resultsTypes = new WasmBridge.ExtismValType[]{WasmBridge.ExtismValType.I64};

        WasmOtoroshiHostFunction[] functions = {
                new WasmOtoroshiHostFunction<>(
                        "hello_world",
                        parametersTypes,
                        resultsTypes,
                        (plugin, params, returns, data) -> {
                            display(plugin);
                            System.out.println("Hello from Java Host Function!");
                        },
                        Optional.empty()
                ).withNamespace("env")
        };

        WasmOtoroshiInstance instance = template.instantiate(engine, functions, new WasmOtoroshiLinearMemory[0], true);

        instance.extismCall("execute", "".getBytes(StandardCharsets.UTF_8));

        instance.free();
        template.free();
        engine.free();
    }

    @Test
    public void shouldGetMemoryBounds() {
        WasmOtoroshiEngine engine = new WasmOtoroshiEngine();

        Manifest manifest = new Manifest(Collections.singletonList(CODE.pathWasmWebAssemblyFunctionSource()), new MemoryOptions(4));

        WasmOtoroshiTemplate template = new WasmOtoroshiTemplate(engine, "template-id", manifest);

        WasmBridge.ExtismValType[] parametersTypes = new WasmBridge.ExtismValType[]{WasmBridge.ExtismValType.I64};
        WasmBridge.ExtismValType[] resultsTypes = new WasmBridge.ExtismValType[]{WasmBridge.ExtismValType.I64};

        WasmOtoroshiHostFunction[] functions = {
                new WasmOtoroshiHostFunction<>(
                        "hello_world",
                        parametersTypes,
                        resultsTypes,
                        (plugin, params, returns, data) -> {
                            System.out.println("Hello from Java Host Function!");
                        },
                        Optional.empty()
                ).withNamespace("env")
        };

        WasmOtoroshiInstance instance = template.instantiate(engine, functions, new WasmOtoroshiLinearMemory[0], true);
        instance.extismCall("execute", "".getBytes(StandardCharsets.UTF_8));
        System.out.println(instance.getMemorySize());

        instance.free();
        template.free();
        engine.free();
    }
}
