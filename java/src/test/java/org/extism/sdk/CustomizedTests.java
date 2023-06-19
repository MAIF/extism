package org.extism.sdk;

import org.extism.sdk.customized.*;
import org.extism.sdk.customized.ExtismFunction;
import org.extism.sdk.customized.HostFunction;
import org.extism.sdk.manifest.Manifest;
import org.extism.sdk.customized.LinearMemory;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.extism.sdk.TestWasmSources.CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomizedTests {

    @Test
    public void shouldWorks() {
        Engine engine = new Engine();

        Manifest manifest = new Manifest(Collections.singletonList(CODE.pathWasmWebAssemblyFunctionSource()));

        Template template = new Template(engine, manifest);

        Bridge.ExtismValType[] parametersTypes = new Bridge.ExtismValType[]{Bridge.ExtismValType.I64};
        Bridge.ExtismValType[] resultsTypes = new Bridge.ExtismValType[]{Bridge.ExtismValType.I64};

        ExtismFunction helloWorldFunction = (plugin, params, returns, data) -> {
            System.out.println("Hello from Java Host Function!");
        };

        HostFunction f = new HostFunction<>(
                "hello_world",
                parametersTypes,
                resultsTypes,
                helloWorldFunction,
                Optional.empty()
        ).withNamespace("env");

        HostFunction[] functions = {f};

        List<Integer> test = new ArrayList<>(500);
        for (int i = 0; i < 500; i++) {
            test.add(i);
        }

        test.parallelStream().forEach(number -> {
            Instance instance = template.instantiate(engine, functions, new LinearMemory[0], true);

            Parameters params = new Parameters(2)
                .pushInts(2, 3);

            Results result = instance.call("add", params, 1);
            assertEquals(result.getValue(0).v.i32, 5);

            instance.freeResults(result);
            instance.free();
        });

        template.free();
        engine.free();
    }

    @Test
    public void shouldExistmCallWorks() {
        Engine engine = new Engine();

        Manifest manifest = new Manifest(Collections.singletonList(CODE.pathWasmWebAssemblyFunctionSource()));

        Template template = new Template(engine, manifest);

        Bridge.ExtismValType[] parametersTypes = new Bridge.ExtismValType[]{Bridge.ExtismValType.I64};
        Bridge.ExtismValType[] resultsTypes = new Bridge.ExtismValType[]{Bridge.ExtismValType.I64};

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

        Instance instance = template.instantiate(engine, functions, new LinearMemory[0], true);

        instance.extismCall("execute", "".getBytes(StandardCharsets.UTF_8));

        instance.free();
        template.free();
        engine.free();
    }

    @Test
    public void shouldInvokeNativeFunction() {
        Manifest manifest = new Manifest(Arrays.asList(CODE.pathWasmWebAssemblyFunctionSource()));
        String functionName = "add";

        Engine engine = new Engine();

        Template template = new Template(engine, manifest);

        Parameters params = new Parameters(2)
                .pushInts(2, 3);

        Instance plugin = template.instantiate(engine, null, null, true);
        Results result = plugin.call(functionName, params, 1);

        assertEquals(result.getValues()[0].v.i32, 5);

        plugin.freeResults(result);
        plugin.free();
        template.free();
        engine.free();
    }
}
