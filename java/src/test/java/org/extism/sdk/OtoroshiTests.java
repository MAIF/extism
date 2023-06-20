package org.extism.sdk;

import org.extism.sdk.otoroshi.*;
import org.extism.sdk.otoroshi.OtoroshiExtismFunction;
import org.extism.sdk.otoroshi.OtoroshiHostFunction;
import org.extism.sdk.otoroshi.OtoroshiResults;
import org.extism.sdk.otoroshi.OtoroshiParameters;
import org.extism.sdk.manifest.Manifest;
import org.extism.sdk.otoroshi.OtoroshiLinearMemory;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.extism.sdk.TestWasmSources.CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OtoroshiTests {

    @Test
    public void shouldWorks() {
        OtoroshiEngine engine = new OtoroshiEngine();

        Manifest manifest = new Manifest(Collections.singletonList(CODE.getRawAdditionPath()));

        OtoroshiTemplate template = new OtoroshiTemplate(engine, manifest);

        Bridge.ExtismValType[] parametersTypes = new Bridge.ExtismValType[]{Bridge.ExtismValType.I64};
        Bridge.ExtismValType[] resultsTypes = new Bridge.ExtismValType[]{Bridge.ExtismValType.I64};

        OtoroshiExtismFunction helloWorldFunction = (plugin, params, returns, data) -> {
            System.out.println("Hello from Java Host Function!");
        };

        OtoroshiHostFunction f = new OtoroshiHostFunction<>(
                "hello_world",
                parametersTypes,
                resultsTypes,
                helloWorldFunction,
                Optional.empty()
        ).withNamespace("env");

        OtoroshiHostFunction[] functions = {f};

        List<Integer> test = new ArrayList<>(500);
        for (int i = 0; i < 500; i++) {
            test.add(i);
        }

        test.parallelStream().forEach(number -> {
            OtoroshiInstance instance = template.instantiate(engine, functions, new OtoroshiLinearMemory[0], true);

            OtoroshiParameters params = new OtoroshiParameters(2)
                .pushInts(2, 3);

            OtoroshiResults result = instance.call("add", params, 1);
            assertEquals(result.getValue(0).v.i32, 5);

            instance.freeResults(result);
            instance.free();
        });

        template.free();
        engine.free();
    }

    @Test
    public void shouldExistmCallWorks() {
        OtoroshiEngine engine = new OtoroshiEngine();

        Manifest manifest = new Manifest(Collections.singletonList(CODE.pathWasmWebAssemblyFunctionSource()));

        OtoroshiTemplate template = new OtoroshiTemplate(engine, manifest);

        Bridge.ExtismValType[] parametersTypes = new Bridge.ExtismValType[]{Bridge.ExtismValType.I64};
        Bridge.ExtismValType[] resultsTypes = new Bridge.ExtismValType[]{Bridge.ExtismValType.I64};

        OtoroshiHostFunction[] functions = {
                new OtoroshiHostFunction<>(
                        "hello_world",
                        parametersTypes,
                        resultsTypes,
                        (plugin, params, returns, data) -> {
                            System.out.println("Hello from Java Host Function!");
                        },
                        Optional.empty()
                ).withNamespace("env")
        };

        OtoroshiInstance instance = template.instantiate(engine, functions, new OtoroshiLinearMemory[0], true);

        instance.extismCall("execute", "".getBytes(StandardCharsets.UTF_8));

        instance.free();
        template.free();
        engine.free();
    }

    @Test
    public void shouldInvokeNativeFunction() {
        Manifest manifest = new Manifest(Arrays.asList(CODE.getRawAdditionPath()));
        String functionName = "add";

        OtoroshiEngine engine = new OtoroshiEngine();

        OtoroshiTemplate template = new OtoroshiTemplate(engine, manifest);

        OtoroshiParameters params = new OtoroshiParameters(2)
                .pushInts(2, 3);

        OtoroshiInstance plugin = template.instantiate(engine, null, null, true);
        OtoroshiResults result = plugin.call(functionName, params, 1);

        assertEquals(result.getValues()[0].v.i32, 5);

        plugin.freeResults(result);
        plugin.free();
        template.free();
        engine.free();
    }
}
