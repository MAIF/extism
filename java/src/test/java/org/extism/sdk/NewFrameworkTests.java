package org.extism.sdk;

import org.extism.sdk.framework.*;
import org.extism.sdk.manifest.Manifest;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.extism.sdk.TestWasmSources.CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NewFrameworkTests {

    @Test
    public void shouldWorks() throws Exception {
        Engine engine = new Engine();

        Manifest manifest = new Manifest(Collections.singletonList(CODE.pathWasmWebAssemblyFunctionSource()));

        Template template = new Template(engine, manifest);

        NewFramework.ExtismValType[] parametersTypes = new NewFramework.ExtismValType[]{NewFramework.ExtismValType.I64};
        NewFramework.ExtismValType[] resultsTypes = new NewFramework.ExtismValType[]{NewFramework.ExtismValType.I64};

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
        for (int i=0; i<500; i++) {
            test.add(i);
        }

        test.parallelStream().forEach(number -> {
            long sT = System.currentTimeMillis();
            Instance instance = template.instantiate(engine, functions, new LinearMemory[0], true);
            long eT = System.currentTimeMillis();
            System.out.println("New plugin: " + (eT - sT) + "ms");

//            Parameters params = new Parameters(2)
//                .pushInts(2, 3);

//            Results result = instance.call("add", params, 1);

            instance.extismCall("execute", "COUCOU".getBytes(StandardCharsets.UTF_8));

//            assertEquals(result.getValue(0).v.i32, 5);

//            instance.freeResults(result);
            instance.free();
        });
    }
}
