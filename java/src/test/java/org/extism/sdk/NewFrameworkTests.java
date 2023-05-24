package org.extism.sdk;

import org.extism.sdk.framework.Engine;
import org.extism.sdk.framework.Instance;
import org.extism.sdk.framework.Template;
import org.extism.sdk.manifest.Manifest;
import org.extism.sdk.parameters.IntegerParameter;
import org.extism.sdk.parameters.Parameters;
import org.extism.sdk.parameters.Results;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.extism.sdk.TestWasmSources.CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NewFrameworkTests {

    @Test
    public void shouldWorks() {
        Engine engine = new Engine();

        Manifest manifest = new Manifest(Arrays.asList(CODE.pathWasmWebAssemblyFunctionSource()));

        Template template = new Template(engine, manifest);

        LibExtism.ExtismValType[] parametersTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};
        LibExtism.ExtismValType[] resultsTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};

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

            Parameters params = new Parameters(2);
            IntegerParameter builder = new IntegerParameter();
            builder.add(params, 2, 0);
            builder.add(params, 3, 1);

            Results result = instance.call("add", params, 1);

            assertEquals(result.getValue(0).v.i32, 5);

            instance.freeResults(result);
            instance.free();
        });
    }
}
